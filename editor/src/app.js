// Melviz Editor
//
// A browser-based YAML editor for Melviz dashboards:
//   - Left split panel: CodeMirror 6 YAML editor with syntax highlighting
//   - Right split panel: the @melviz/webapp package (webapp/dist) rendering
//     the current dashboard, refreshed in real time (500 ms debounce)
//   - Open files from disk, download the current file, and pull files in
//     from a GitHub repository
//   - Current file state is saved to (and restored from) browser storage
//
// CodeMirror 6 is loaded from the bundled `codemirror.js` script (built by
// `scripts/build-codemirror.js`), which exposes `window.CodeMirror6`.
(function () {
    'use strict';

    /* =========================================================================
     * Constants & state
     * ========================================================================= */

    const REFRESH_DELAY_MS = 500;   // live-refresh debounce
    const HISTORY_KEY = 'melviz.editor.history';
    const HISTORY_MAX = 10;
    const STORAGE_KEY_PREFIX = 'melviz.editor.file.';
    const THEME_KEY = 'melviz.editor.theme';
    const PANEL_WIDTH_KEY = 'melviz.editor.codePanelWidth';
    const GH_TOKEN_KEY = 'melviz.editor.github.token';

    const WEBAPP_BASE = 'melviz-webapp/index.html';
    const WEBAPP_QUERY = '?import=';

    // In-memory "virtual" dashboard registry. The dev/serve server exposes
    // /dashboards/editor/<name>.dash.yaml for each entry of this map, and the
    // webapp iframe is pointed at it via the `import` query parameter.
    const virtualDashboards = new Map(); // name -> { yaml, fileName, mtime }

    const state = {
        fileName: 'dashboard.yaml',
        history: [],
        github: {
            token: '',
            owner: '',
            repo: '',
            branch: '',
            files: [],
        },
    };

    /* =========================================================================
     * DOM references
     * ========================================================================= */

    const $ = (id) => document.getElementById(id);

    const els = {
        fileInput: document.createElement('input'),
        fileName: $('file-name'),
        fileStatus: $('file-status'),
        editorEl: $('editor'),

        workspace: $('workspace'),
        editorPanel: $('editor-panel'),
        previewPanel: $('preview-panel'),
        panelResizer: $('panel-resizer'),

        stage: $('stage'),
        stageLoading: $('stage-loading'),
        dashboardIframe: $('dashboard-iframe'),

        // topbar actions
        newFile: $('new-file'),
        openFile: $('open-file'),
        downloadFile: $('download-file'),
        githubBtn: $('github-btn'),
        saveFile: $('save-file'),
        themeToggle: $('themeToggle'),
        reloadPreview: $('reload-preview'),
        refreshDot: $('refresh-dot'),

        // github overlay
        githubOverlay: $('github-overlay'),
        githubClose: $('github-close'),
        githubForm: $('github-form'),
        githubToken: $('github-token'),
        githubOwner: $('github-owner'),
        githubRepo: $('github-repo'),
        githubBranch: $('github-branch'),
        githubLoadRepo: $('github-load-repo'),
        githubResults: $('github-results'),
        githubResultsTitle: $('github-results-title'),
        githubRefreshFiles: $('github-refresh-files'),
        githubFilter: $('github-filter'),
        githubFiles: $('github-files'),
        githubError: $('github-error'),
    };

    let cm = null;                  // CodeMirror 6 API (from window.CodeMirror6)
    let editor = null;              // CodeMirror EditorView
    let refreshTimer = null;        // debounce timer for live refresh
    let lastRendered = null;        // content hash of last rendered dashboard
    let statusTimeout = null;

    /* =========================================================================
     * Utilities
     * ========================================================================= */

    function escapeHtml(text) {
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }

    // Simple, stable hash used to know whether the preview needs reloading.
    function hashContent(text) {
        let h1 = 0xdeadbeef, h2 = 0x41c6ce57;
        for (let i = 0; i < text.length; i++) {
            const ch = text.charCodeAt(i);
            h1 = Math.imul(h1 ^ ch, 2654435761);
            h2 = Math.imul(h2 ^ ch, 1597334677);
        }
        h1 = Math.imul(h1 ^ (h1 >>> 16), 2246822507) ^ Math.imul(h2 ^ (h2 >>> 13), 3266489909);
        h2 = Math.imul(h2 ^ (h2 >>> 16), 2246822507) ^ Math.imul(h1 ^ (h1 >>> 13), 3266489909);
        return (h2 >>> 0).toString(16) + (h1 >>> 0).toString(16);
    }

    function fileNameFromPath(path) {
        const parts = String(path).split('/');
        return parts[parts.length - 1] || 'dashboard.yaml';
    }

    function setFileName(name) {
        state.fileName = (name || 'dashboard.yaml').trim() || 'dashboard.yaml';
        // Keep a clean name for the virtual registry.
        state.fileName = state.fileName.replace(/[^\w.\-]/g, '_');
        els.fileName.textContent = state.fileName;
    }

    function showStatus(message, kind = 'info', timeout = 2500) {
        els.fileStatus.textContent = message;
        els.fileStatus.className = 'file-status status-' + kind;
        clearTimeout(statusTimeout);
        if (timeout > 0) {
            statusTimeout = setTimeout(() => {
                els.fileStatus.textContent = '';
                els.fileStatus.className = 'file-status';
            }, timeout);
        }
    }

    /* =========================================================================
     * Persistence (browser storage)
     *
     * "Save" keeps the current file state in localStorage so the editor
     * restores it on the next visit. A small history of recently opened
     * files is kept as well.
     * ========================================================================= */

    function storageKey(name) {
        return STORAGE_KEY_PREFIX + name;
    }

    function readStorage(key) {
        try {
            return localStorage.getItem(key);
        } catch (e) {
            return null;
        }
    }

    function writeStorage(key, value) {
        try {
            localStorage.setItem(key, value);
            return true;
        } catch (e) {
            console.warn('Unable to persist to browser storage:', e);
            return false;
        }
    }

    function loadCurrentFile() {
        const saved = readStorage(storageKey(state.fileName));
        if (saved !== null) {
            try {
                const parsed = JSON.parse(saved);
                if (typeof parsed.yaml === 'string') {
                    return parsed;
                }
            } catch (e) {
                /* fall through to null */
            }
        }
        return null;
    }

    function saveToStorage(showMessage) {
        const docText = editor ? editor.state.doc.toString() : '';
        const ok = writeStorage(storageKey(state.fileName), JSON.stringify({
            yaml: docText,
            savedAt: Date.now(),
        }));
        if (showMessage !== false) {
            showStatus(ok ? 'Saved to browser storage' : 'Could not save (storage unavailable)',
                ok ? 'success' : 'error', 2000);
        }
        pushHistory();
        return ok;
    }

    function pushHistory() {
        const entry = {
            name: state.fileName,
            at: Date.now(),
        };
        state.history = state.history.filter((h) => h.name !== entry.name);
        state.history.unshift(entry);
        state.history = state.history.slice(0, HISTORY_MAX);
        try {
            localStorage.setItem(HISTORY_KEY, JSON.stringify(state.history));
        } catch (e) { /* ignore */ }
    }

    function loadHistory() {
        const raw = readStorage(HISTORY_KEY);
        if (!raw) {
            state.history = [];
            return;
        }
        try {
            const parsed = JSON.parse(raw);
            state.history = Array.isArray(parsed) ? parsed : [];
        } catch (e) {
            state.history = [];
        }
    }

    /* =========================================================================
     * Editor (CodeMirror 6 — YAML with syntax highlighting)
     * ========================================================================= */

    let textareaEl = null;

    function createEditor(initialText) {
        const { EditorState, EditorView, yaml, keymap, searchKeymap } = cm;

        try {
            const editorState = EditorState.create({
                doc: initialText,
                extensions: [
                    yaml(),
                    EditorView.lineWrapping,
                    EditorView.updateListener.of((update) => {
                        if (update.docChanged) {
                            scheduleRefresh();
                        }
                    }),
                    keymap.of([
                        {
                            key: 'Mod-s',
                            run: () => { saveToStorage(); return true; },
                        },
                        ...searchKeymap,
                    ]),
                ],
            });
            editor = new EditorView(els.editorEl, { state: editorState });
        } catch (err) {
            // CodeMirror 6 failed to mount (rare, environment-specific). Fall back
            // to a plain textarea so editing + live preview still work.
            console.warn('CodeMirror failed to initialize, using textarea fallback:', err);
            els.editorEl.classList.add('editor-fallback');
            textareaEl = document.createElement('textarea');
            textareaEl.className = 'cm-fallback-textarea';
            textareaEl.spellcheck = false;
            textareaEl.value = initialText;
            textareaEl.addEventListener('input', () => {
                els.fileStatus.textContent = 'Editing…';
                scheduleRefresh();
            });
            els.editorEl.appendChild(textareaEl);
        }
    }

    function editorDoc() {
        if (textareaEl) {
            return textareaEl.value;
        }
        return editor ? editor.state.doc.toString() : '';
    }

    function setEditorDoc(text) {
        if (textareaEl) {
            textareaEl.value = text;
            return;
        }
        if (editor) {
            editor.dispatch({
                changes: { from: 0, to: editor.state.doc.length, insert: text },
            });
        }
    }

    /* =========================================================================
     * Live preview (right split panel) — the @melviz/webapp package
     *
     * The preview iframe loads the webapp with ?import=<name>, where <name>
     * is a virtual dashboard. The dev/serve server serves the current editor
     * content at /dashboards/editor/<name>.dash.yaml, so reloading the iframe
     * always picks up the latest YAML. The refresh is debounced to
     * REFRESH_DELAY_MS (500 ms) so typing feels live without spamming.
     * ========================================================================= */

    function currentDashboardName() {
        return state.fileName.replace(/\.(ya?ml)$/i, '');
    }

    // Relative path from the webapp (melviz-webapp/index.html) back to the
    // editor's virtual-dashboard endpoint. The webapp fetches
    // `clientModelBaseUrl + importID`, so passing this relative path lands on
    // /dashboards/editor/<name>.dash.yaml.
    function virtualDashboardUrl() {
        return `../dashboards/editor/${currentDashboardName()}.dash.yaml`;
    }

    function registerVirtualDashboard() {
        const name = currentDashboardName();
        virtualDashboards.set(name, {
            yaml: editorDoc(),
            fileName: state.fileName,
            mtime: Date.now(),
        });
        // Push the content to the server so the iframe reload can fetch it.
        try {
            fetch('/_editor/dashboards', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name,
                    yaml: editorDoc(),
                    fileName: state.fileName,
                }),
            }).catch(() => { /* dev server may not be running */ });
        } catch (e) { /* ignore */ }
    }

    function scheduleRefresh() {
        registerVirtualDashboard();
        if (refreshTimer) {
            clearTimeout(refreshTimer);
        }
        els.refreshDot.classList.add('pending');
        refreshTimer = setTimeout(() => {
            refreshTimer = null;
            reloadPreview();
        }, REFRESH_DELAY_MS);
    }

    function webappUrl() {
        return `${WEBAPP_BASE}${WEBAPP_QUERY}${encodeURIComponent(virtualDashboardUrl())}`;
    }

    function reloadPreview() {
        const content = editorDoc();
        const hash = hashContent(content);
        // Nothing changed since the last render → skip the reload.
        if (hash === lastRendered) {
            els.refreshDot.classList.remove('pending');
            return;
        }
        lastRendered = hash;
        els.stageLoading.classList.remove('hidden');
        // Bust the cache so the webapp always re-fetches the fresh YAML.
        els.dashboardIframe.src = webappUrl() + '&_t=' + Date.now();
    }

    els.dashboardIframe.addEventListener('load', () => {
        els.stageLoading.classList.add('hidden');
        els.refreshDot.classList.remove('pending');
    });

    /* =========================================================================
     * Files: New / Open / Download
     * ========================================================================= */

    function newFile() {
        const name = window.prompt(
            'New dashboard file name',
            'dashboard.yaml'
        );
        if (name === null) {
            return;
        }
        setFileName(name.trim() || 'dashboard.yaml');
        setEditorDoc(defaultTemplate());
        saveToStorage(false);
        scheduleRefresh();
        showStatus('New file created', 'success', 2000);
    }

    function defaultTemplate() {
        return [
            'global:',
            '  displayer:',
            '    chart:',
            '      height: 320',
            '      resizable: true',
            'datasets:',
            '  - uuid: hello',
            '    content: >-',
            '      [',
            '        ["name", "message"],',
            '        ["Melviz", "Hello, editor!"],',
            '        ["YAML", "Edit me on the left"],',
            '      ]',
            'pages:',
            '  - components:',
            '      - markdown: "# My dashboard"',
            '      - displayer:',
            '          type: table',
            '          lookup:',
            '            uuid: hello',
            '',
        ].join('\n');
    }

    function openFile() {
        els.fileInput.accept = '.yaml,.yml,.json,.dash.yaml,.dash.yml';
        els.fileInput.onchange = () => {
            const file = els.fileInput.files && els.fileInput.files[0];
            if (!file) {
                return;
            }
            const reader = new FileReader();
            reader.onload = () => {
                setFileName(file.name);
                setEditorDoc(String(reader.result || ''));
                saveToStorage(false);
                scheduleRefresh();
                showStatus(`Opened ${file.name}`, 'success', 2500);
            };
            reader.onerror = () => showStatus('Could not read file', 'error', 3000);
            reader.readAsText(file);
            els.fileInput.value = '';
        };
        els.fileInput.click();
    }

    function downloadFile() {
        const blob = new Blob([editorDoc()], { type: 'text/yaml;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = state.fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(() => URL.revokeObjectURL(url), 1000);
        showStatus(`Downloaded ${state.fileName}`, 'success', 2000);
    }

    /* =========================================================================
     * GitHub integration
     *
     * Uses the public GitHub REST API (no SDK). A token is optional — it
     * unlocks private repos and raises rate limits. The user picks an owner,
     * repo and branch, we list YAML files in the repository tree and let them
     * click one to load it into the editor.
     * ========================================================================= */

    const GH_API = 'https://api.github.com';

    function ghHeaders() {
        const h = {
            'Accept': 'application/vnd.github+json',
            'X-GitHub-Api-Version': '2022-11-28',
        };
        const token = (els.githubToken.value || '').trim();
        if (token) {
            h.Authorization = `Bearer ${token}`;
        }
        return h;
    }

    function openGitHub() {
        els.githubToken.value = readStorage(GH_TOKEN_KEY) || '';
        els.githubOwner.value = state.github.owner || '';
        els.githubRepo.value = state.github.repo || '';
        els.githubBranch.value = state.github.branch || '';
        els.githubResults.hidden = true;
        els.githubError.hidden = true;
        els.githubFiles.innerHTML = '';
        els.githubFilter.value = '';
        showOverlay(true);
        setTimeout(() => els.githubOwner.focus(), 50);
    }

    function showOverlay(show) {
        els.githubOverlay.hidden = !show;
    }

    async function loadRepository() {
        const owner = els.githubOwner.value.trim();
        const repo = els.githubRepo.value.trim();
        const branch = els.githubBranch.value.trim();
        const token = els.githubToken.value.trim();

        state.github.owner = owner;
        state.github.repo = repo;
        state.github.branch = branch;
        if (token) {
            writeStorage(GH_TOKEN_KEY, token);
        }

        if (!owner || !repo) {
            showGitHubError('Enter an owner and a repository name.');
            return;
        }

        setGitHubLoading(true);
        hideGitHubError();

        try {
            // Resolve the default branch when none was given.
            let ref = branch;
            if (!ref) {
                const repoRes = await ghFetch(`${GH_API}/repos/${owner}/${repo}`);
                ref = repoRes.default_branch;
                els.githubBranch.value = ref;
            }

            const treeRes = await ghFetch(
                `${GH_API}/repos/${owner}/${repo}/git/trees/${ref}?recursive=1`
            );
            const yamlExt = /\.(ya?ml)$/i;
            const files = (treeRes.tree || [])
                .filter((t) => t.type === 'blob' && yamlExt.test(t.path))
                .map((t) => t.path);

            state.github.files = files;
            els.githubResultsTitle.textContent =
                `${owner}/${repo} · ${ref} · ${files.length} YAML file${files.length === 1 ? '' : 's'}`;
            els.githubResults.hidden = false;
            renderGitHubFiles('');
        } catch (e) {
            console.error('GitHub load failed', e);
            showGitHubError(humanizeGitHubError(e));
        } finally {
            setGitHubLoading(false);
        }
    }

    async function ghFetch(url) {
        const res = await fetch(url, { headers: ghHeaders() });
        if (res.status === 401) {
            throw new Error('Unauthorized — check your GitHub token.');
        }
        if (res.status === 403) {
            throw new Error('Rate limited or forbidden by GitHub. Provide a token or try again later.');
        }
        if (res.status === 404) {
            throw new Error('Not found — check the owner, repo and branch.');
        }
        if (!res.ok) {
            throw new Error(`GitHub responded with HTTP ${res.status}.`);
        }
        return res.json();
    }

    function humanizeGitHubError(e) {
        return (e && e.message) || String(e);
    }

    function setGitHubLoading(loading) {
        els.githubLoadRepo.disabled = loading;
        els.githubLoadRepo.textContent = loading ? 'Loading…' : 'Load repository';
        if (loading) {
            els.githubFiles.innerHTML = '<div class="gh-empty">Loading file list…</div>';
            els.githubResults.hidden = false;
        }
    }

    function showGitHubError(message) {
        els.githubError.textContent = message;
        els.githubError.hidden = false;
    }

    function hideGitHubError() {
        els.githubError.hidden = true;
        els.githubError.textContent = '';
    }

    function renderGitHubFiles(filter) {
        const term = (filter || '').trim().toLowerCase();
        const files = state.github.files.filter((f) => !term || f.toLowerCase().includes(term));
        els.githubFiles.innerHTML = '';

        if (files.length === 0) {
            els.githubFiles.innerHTML =
                `<div class="gh-empty">${state.github.files.length === 0
                    ? 'No YAML files found in this repository.'
                    : 'No files match your filter.'}</div>`;
            return;
        }

        // Cap the visible list to keep the DOM small on huge repos.
        const visible = files.slice(0, 500);
        visible.forEach((file) => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'gh-file';
            btn.innerHTML = `<span class="gh-file-path">${escapeHtml(file)}</span>`;
            btn.title = 'Load into the editor';
            btn.addEventListener('click', () => openGitHubFile(file));
            els.githubFiles.appendChild(btn);
        });

        if (files.length > visible.length) {
            const more = document.createElement('div');
            more.className = 'gh-more';
            more.textContent = `…and ${files.length - visible.length} more — refine the filter above.`;
            els.githubFiles.appendChild(more);
        }
    }

    async function openGitHubFile(filePath) {
        const owner = state.github.owner;
        const repo = state.github.repo;
        let branch = state.github.branch;

        setGitHubLoading(true);
        try {
            if (!branch) {
                const repoRes = await ghFetch(`${GH_API}/repos/${owner}/${repo}`);
                branch = repoRes.default_branch;
            }
            const res = await fetch(
                `${GH_API}/repos/${owner}/${repo}/contents/${encodeURIComponent(filePath)}?ref=${encodeURIComponent(branch)}`,
                { headers: ghHeaders() }
            );
            if (!res.ok) {
                throw new Error(`Could not download the file (HTTP ${res.status}).`);
            }
            const json = await res.json();
            // The contents API returns base64-encoded content.
            const text = base64Decode(json.content || '');
            setFileName(fileNameFromPath(filePath));
            setEditorDoc(text);
            saveToStorage(false);
            scheduleRefresh();
            showOverlay(false);
            showStatus(`Loaded ${filePath} from ${owner}/${repo}`, 'success', 3000);
        } catch (e) {
            console.error('GitHub file load failed', e);
            showGitHubError(humanizeGitHubError(e));
        } finally {
            setGitHubLoading(false);
        }
    }

    function base64Decode(b64) {
        const cleaned = String(b64).replace(/\s/g, '');
        const byteString = atob(cleaned);
        const bytes = new Uint8Array(byteString.length);
        for (let i = 0; i < byteString.length; i++) {
            bytes[i] = byteString.charCodeAt(i);
        }
        return new TextDecoder('utf-8').decode(bytes);
    }

    /* =========================================================================
     * Theme
     * ========================================================================= */

    function applyTheme(theme, persist = true) {
        document.documentElement.dataset.theme = theme;
        els.themeToggle.title =
            theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme';
        if (persist) {
            writeStorage(THEME_KEY, theme);
            // The preview iframe reads the same key on boot.
            scheduleRefresh();
        }
    }

    function setupTheme() {
        let theme = 'dark';
        try {
            theme = localStorage.getItem(THEME_KEY) || 'dark';
        } catch (e) { /* ignore */ }
        applyTheme(theme, false);
        els.themeToggle.addEventListener('click', () => {
            const next =
                document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
            applyTheme(next);
        });
    }

    /* =========================================================================
     * Panel resizing
     * ========================================================================= */

    const DEFAULT_PANEL_WIDTH = 460;
    const MIN_PANEL_WIDTH = 260;
    const MIN_PREVIEW_WIDTH = 320;

    function getPanelWidth() {
        return els.editorPanel.getBoundingClientRect().width || DEFAULT_PANEL_WIDTH;
    }

    function clampPanelWidth(width) {
        const max = Math.max(MIN_PANEL_WIDTH, els.workspace.clientWidth - MIN_PREVIEW_WIDTH);
        return Math.min(Math.max(width, MIN_PANEL_WIDTH), max);
    }

    function applyPanelWidth(width, persist = true) {
        const clamped = clampPanelWidth(width);
        els.editorPanel.style.width = `${clamped}px`;
        els.panelResizer.setAttribute('aria-valuenow', String(Math.round(clamped)));
        if (persist) {
            writeStorage(PANEL_WIDTH_KEY, String(Math.round(clamped)));
        }
        return clamped;
    }

    function restorePanelWidth() {
        let width = DEFAULT_PANEL_WIDTH;
        const saved = parseInt(readStorage(PANEL_WIDTH_KEY), 10);
        if (!Number.isNaN(saved)) {
            width = saved;
        }
        applyPanelWidth(width, false);
    }

    function setupPanelResizer() {
        let dragging = false;
        let startX = 0;
        let startWidth = 0;

        els.panelResizer.addEventListener('pointerdown', (event) => {
            event.preventDefault();
            dragging = true;
            startX = event.clientX;
            startWidth = getPanelWidth();
            document.body.classList.add('resizing');
            els.panelResizer.setPointerCapture(event.pointerId);
        });
        els.panelResizer.addEventListener('pointermove', (event) => {
            if (!dragging) return;
            applyPanelWidth(startWidth + (event.clientX - startX));
        });
        const end = (event) => {
            if (!dragging) return;
            dragging = false;
            document.body.classList.remove('resizing');
            try { els.panelResizer.releasePointerCapture(event.pointerId); } catch (e) { /* ignore */ }
        };
        els.panelResizer.addEventListener('pointerup', end);
        els.panelResizer.addEventListener('pointercancel', end);
        els.panelResizer.addEventListener('dblclick', () => applyPanelWidth(DEFAULT_PANEL_WIDTH));
        els.panelResizer.addEventListener('keydown', (event) => {
            const step = event.shiftKey ? 64 : 16;
            if (event.key === 'ArrowLeft') { applyPanelWidth(getPanelWidth() - step); event.preventDefault(); }
            else if (event.key === 'ArrowRight') { applyPanelWidth(getPanelWidth() + step); event.preventDefault(); }
            else if (event.key === 'Home') { applyPanelWidth(MIN_PANEL_WIDTH); event.preventDefault(); }
            else if (event.key === 'End') { applyPanelWidth(clampPanelWidth(Number.MAX_SAFE_INTEGER)); event.preventDefault(); }
        });

        window.addEventListener('resize', () => applyPanelWidth(getPanelWidth()));
    }

    /* =========================================================================
     * Actions & boot
     * ========================================================================= */

    function setupActions() {
        els.newFile.addEventListener('click', newFile);
        els.openFile.addEventListener('click', openFile);
        els.downloadFile.addEventListener('click', downloadFile);
        els.githubBtn.addEventListener('click', openGitHub);
        els.saveFile.addEventListener('click', () => saveToStorage());
        els.reloadPreview.addEventListener('click', () => {
            // Force a reload even if the content hash didn't change.
            lastRendered = null;
            reloadPreview();
        });

        // GitHub overlay
        els.githubClose.addEventListener('click', () => showOverlay(false));
        els.githubOverlay.addEventListener('click', (event) => {
            if (event.target === els.githubOverlay) showOverlay(false);
        });
        els.githubLoadRepo.addEventListener('click', loadRepository);
        els.githubRefreshFiles.addEventListener('click', loadRepository);
        els.githubFilter.addEventListener('input', (event) => renderGitHubFiles(event.target.value));
        els.githubForm.addEventListener('submit', (event) => {
            event.preventDefault();
            loadRepository();
        });

        // Global keyboard shortcuts
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && !els.githubOverlay.hidden) {
                showOverlay(false);
                return;
            }
            const target = document.activeElement;
            const inField =
                target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA');
            if (inField) return;
            if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'o') {
                event.preventDefault();
                openFile();
            } else if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'd') {
                event.preventDefault();
                downloadFile();
            }
        });
    }

    function boot() {
        cm = window.CodeMirror6;
        if (!cm) {
            console.error('CodeMirror bundle (window.CodeMirror6) is missing — is codemirror.js loaded?');
            return;
        }

        setupTheme();
        restorePanelWidth();
        setupPanelResizer();
        setupActions();
        loadHistory();

        // Restore the last opened file name (if any) so a returning user
        // lands on the file they were editing.
        const lastEntry = state.history[0];
        if (lastEntry && lastEntry.name) {
            setFileName(lastEntry.name);
        } else {
            setFileName('dashboard.yaml');
        }

        // Restore content from browser storage, or fall back to a template.
        const saved = loadCurrentFile();
        const initialText = saved && typeof saved.yaml === 'string'
            ? (saved.yaml || defaultTemplate())
            : defaultTemplate();
        setEditorDoc(initialText);
        createEditor(initialText);

        // Point the preview at the (initial) dashboard and render it.
        registerVirtualDashboard();
        lastRendered = null;
        reloadPreview();

        showStatus(
            saved ? `Restored ${state.fileName} from browser storage` : 'New editor session',
            'info', 3000
        );
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boot);
    } else {
        boot();
    }
})();
