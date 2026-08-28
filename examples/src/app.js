// Melviz Examples Gallery Application
(function () {
    'use strict';

    /* =========================================================================
     * Constants & state
     * ========================================================================= */
    const DEFAULT_PANEL_WIDTH = 420;   // px
    const MIN_PANEL_WIDTH = 240;       // px
    const MIN_STAGE_WIDTH = 320;       // px (minimum space kept for the preview)
    const THEME_KEY = 'melviz.examples.theme';
    const PANEL_WIDTH_KEY = 'melviz.examples.codePanelWidth';
    const WEBAPP_URL = 'melviz-webapp/index.html?import=';

    let samplesData = null;
    let currentDashboard = null;
    let currentSource = null;
    let searchOpen = false;
    let searchActiveIndex = -1;

    /* =========================================================================
     * DOM references
     * ========================================================================= */
    const $ = (id) => document.getElementById(id);

    const els = {
        brand: $('brand'),
        themeToggle: $('theme-toggle'),
        searchOpen: $('search-open'),
        searchOverlay: $('search-overlay'),
        searchInput: $('search-input'),
        searchResults: $('search-results'),
        searchClose: $('search-close'),

        homeView: $('home-view'),
        heroStats: $('hero-stats'),
        categoryList: $('category-list'),

        viewerView: $('viewer-view'),
        viewerBody: $('viewer-body'),
        backBtn: $('back-btn'),
        crumbCategory: $('crumb-category'),
        crumbName: $('crumb-name'),
        sourceToggle: $('source-toggle'),
        copyYaml: $('copy-yaml'),
        copyYamlLabel: $('copy-yaml-label'),
        openNewWindow: $('open-new-window'),
        reloadDashboard: $('reload-dashboard'),

        stage: $('stage'),
        stageLoading: $('stage-loading'),
        dashboardIframe: $('dashboard-iframe'),

        codePanel: $('code-panel'),
        codeFile: $('code-file'),
        codeScroll: $('code-scroll'),
        code: $('code'),
        closeCode: $('close-code'),
        copyCode: $('copy-code'),
        panelResizer: $('panel-resizer'),
    };

    /* =========================================================================
     * YAML syntax highlighting (dependency free)
     *
     * A small line-based YAML tokenizer. Each line is tokenized independently
     * and the raw text is HTML-escaped per token, so the output can be safely
     * injected with innerHTML.
     * ========================================================================= */

    // Token groups (order defines precedence; group index -> CSS class)
    const YAML_TOKEN_GROUPS = [
        /"(?:[^"\\]|\\.)*"/.source,                     // 1  double-quoted string
        /'(?:[^']|'')*'/.source,                        // 2  single-quoted string
        /(?<!\S)#[^\n]*/.source,                        // 3  comment
        /^(?:---|\.\.\.)[^\n]*$/.source,               // 4  document marker
        /&[A-Za-z0-9_$][\w$.-]*/.source,               // 5  anchor
        /\*[A-Za-z0-9_$][\w$.-]*/.source,              // 6  alias
        /!!?[A-Za-z0-9_./-]*/.source,                  // 7  tag
        /(?<![\w.])(?:[+-])?(?:0x[0-9A-Fa-f_]+|0o[0-7_]+|0b[0-1_]+|\d[\d_]*(?:\.[\d_]+)?(?:[eE][+-]?[\d_]+)?)\b/.source, // 8  number
        /(?<!\w)(?:true|false|yes|no|on|off)\b/.source, // 9  boolean
        /(?<!\w)(?:~|null|None|Null)\b/.source,          // 10 null
        /(?<![\w$/.-])[\w$-][\w$./-]*(?=\s*:(?:\s|$))/.source, // 11 key
        /(?<=^|\s)-(?=\s|$)/.source,                   // 12 list dash
        /[[\]{},]/.source,                             // 13 flow punctuation
    ];

    const YAML_TOKEN_RE = new RegExp(
        YAML_TOKEN_GROUPS.map((g) => `(${g})`).join('|'),
        'gm'
    );

    const YAML_TOKEN_CLASS = [
        'tk-string', // 1
        'tk-string', // 2
        'tk-comment',// 3
        'tk-doc',    // 4
        'tk-anchor', // 5
        'tk-alias',  // 6
        'tk-tag',    // 7
        'tk-number', // 8
        'tk-bool',   // 9
        'tk-null',   // 10
        'tk-key',    // 11
        'tk-punct',  // 12
        'tk-punct',  // 13
    ];

    function escapeHtml(text) {
        return text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }

    function tokenizeLine(line) {
        if (line === '') {
            return '';
        }
        YAML_TOKEN_RE.lastIndex = 0;
        let out = '';
        let last = 0;
        let match;
        while ((match = YAML_TOKEN_RE.exec(line)) !== null) {
            if (match.index > last) {
                out += escapeHtml(line.slice(last, match.index));
            }
            let cls = 'tk-plain';
            for (let g = 1; g < match.length; g++) {
                if (match[g] !== undefined) {
                    cls = YAML_TOKEN_CLASS[g - 1];
                    break;
                }
            }
            out += `<span class="${cls}">${escapeHtml(match[0])}</span>`;
            last = YAML_TOKEN_RE.lastIndex;
            if (match[0] === '') {
                YAML_TOKEN_RE.lastIndex++;
            }
        }
        out += escapeHtml(line.slice(last));
        return out;
    }

    /**
     * Returns a Set with the indexes of the lines that belong to a block
     * scalar (| or >), so they can be rendered as plain string content.
     */
    function blockScalarLines(lines) {
        const scalar = new Set();
        const headerRe = /^\s*(?:(?:-)?\s*[^\s:#][^:]*:\s*)?[|>][0-9+-]*(?:\s#.*)?$/;
        let activeIndent = -1;

        for (let i = 0; i < lines.length; i++) {
            const line = lines[i];
            const trimmed = line.trim();
            const indent = line.length - line.trimStart().length;

            if (activeIndent >= 0) {
                if (trimmed === '' || indent > activeIndent) {
                    scalar.add(i);
                    continue;
                }
                activeIndent = -1;
            }

            if (trimmed !== '' && headerRe.test(line)) {
                activeIndent = indent;
            }
        }
        return scalar;
    }

    function highlightYaml(source) {
        const lines = source.replace(/\t/g, '    ').split('\n');
        const scalar = blockScalarLines(lines);
        return lines
            .map((line, i) => {
                if (scalar.has(i)) {
                    return `<span class="tk-string">${escapeHtml(line) || '\u200b'}</span>`;
                }
                return tokenizeLine(line);
            })
            .join('\n');
    }

    /* =========================================================================
     * Loading & rendering the home view
     * ========================================================================= */

    async function loadSamples() {
        try {
            const response = await fetch('samples.json');
            samplesData = await response.json();
            renderHeroStats();
            renderCategories();
        } catch (error) {
            console.error('Error loading samples.json:', error);
            els.categoryList.innerHTML =
                '<div class="home-error">Error loading samples.json</div>';
        }
    }

    function renderHeroStats() {
        els.heroStats.innerHTML = `
            <div class="hero-stat">
                <span class="hero-stat-value">${samplesData.totalDashboards}</span>
                <span class="hero-stat-label">dashboards</span>
            </div>
            <div class="hero-stat">
                <span class="hero-stat-value">${samplesData.categories.length}</span>
                <span class="hero-stat-label">categories</span>
            </div>
        `;
    }

    function renderCategories() {
        els.categoryList.innerHTML = '';

        samplesData.categories.forEach((category) => {
            const card = document.createElement('section');
            card.className = 'category-card';

            const head = document.createElement('div');
            head.className = 'category-head';
            head.innerHTML = `
                <span class="category-name">${escapeHtml(category.category)}</span>
                <span class="category-count">${category.dashboards.length}</span>
            `;

            const items = document.createElement('div');
            items.className = 'category-items';

            category.dashboards.forEach((dashboard) => {
                const item = document.createElement('button');
                item.type = 'button';
                item.className = 'dashboard-link';
                item.dataset.path = dashboard.path;
                item.innerHTML = `
                    <span class="dashboard-name">${escapeHtml(dashboard.name)}</span>
                    <span class="dashboard-arrow" aria-hidden="true">›</span>
                `;
                item.addEventListener('click', () => openDashboard(dashboard));
                items.appendChild(item);
            });

            card.appendChild(head);
            card.appendChild(items);
            els.categoryList.appendChild(card);
        });
    }

    /* =========================================================================
     * Viewer view (dashboard + YAML source panel)
     * ========================================================================= */

    function findDashboard(category, path) {
        if (!samplesData) {
            return null;
        }
        for (const categoryGroup of samplesData.categories) {
            for (const dashboard of categoryGroup.dashboards) {
                if (dashboard.path === path) {
                    return dashboard;
                }
            }
        }
        return null;
    }

    function openDashboard(dashboard) {
        currentDashboard = dashboard;
        currentSource = null;

        // Update URL hash (category/path)
        const hash = `${dashboard.category}/${encodeURIComponent(dashboard.path)}`;
        if (window.location.hash !== `#${hash}`) {
            window.location.hash = hash;
        }

        showViewer();
        renderCrumbs(dashboard);
        loadDashboardInIframe(dashboard.path);
        loadDashboardSourceCode(dashboard);
        restorePanelWidth();
    }

    function showViewer() {
        els.homeView.hidden = true;
        els.viewerView.hidden = false;
        showCodePanel(true);
    }

    function showHome() {
        currentDashboard = null;
        currentSource = null;
        els.viewerView.hidden = true;
        els.homeView.hidden = false;
        showCodePanel(false);
        els.dashboardIframe.src = 'about:blank';
    }

    function renderCrumbs(dashboard) {
        els.crumbCategory.textContent = dashboard.category;
        els.crumbName.textContent = dashboard.name;
    }

    function loadDashboardInIframe(dashboardPath) {
        const dashboardUrl = `${window.location.origin}/dashboards/${dashboardPath}`;
        els.stageLoading.classList.remove('hidden');
        els.dashboardIframe.src = WEBAPP_URL + encodeURIComponent(dashboardUrl);
    }

    async function loadDashboardSourceCode(dashboard) {
        try {
            const response = await fetch(`dashboards/${dashboard.path}`);
            currentSource = await response.text();
            renderSourceCode(dashboard, currentSource);
        } catch (error) {
            console.error('Error loading dashboard source:', error);
            currentSource = 'Error loading source code';
            renderSourceCode(dashboard, currentSource);
        }
    }

    function renderSourceCode(dashboard, source) {
        els.codeFile.textContent = dashboard.file || dashboard.name;
        els.code.innerHTML = highlightYaml(source);
        els.codeScroll.scrollTop = 0;
    }

    function showCodePanel(visible) {
        els.codePanel.hidden = !visible;
        if (visible) {
            restorePanelWidth();
        }
    }

    function toggleCodePanel() {
        showCodePanel(els.codePanel.hidden);
    }

    /* =========================================================================
     * Routing (hash based: #category/dashboards/...yaml)
     * ========================================================================= */

    function route() {
        const hash = window.location.hash.slice(1);
        if (!hash) {
            showHome();
            return;
        }
        const [category, encodedPath] = hash.split('/');
        const path = decodeURIComponent(encodedPath || '');
        const dashboard = findDashboard(category, path);
        if (dashboard) {
            openDashboard(dashboard);
        } else {
            showHome();
        }
    }

    /* =========================================================================
     * Code panel: YAML source, copy, resize
     * ========================================================================= */

    function copyYaml() {
        if (currentSource === null) {
            return;
        }
        const label = els.copyYamlLabel;
        const original = label.textContent;

        const done = () => {
            label.textContent = 'Copied!';
            setTimeout(() => {
                label.textContent = original;
            }, 1500);
        };

        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(currentSource).then(done).catch(() => {});
        }
    }

    /* ----------------------------- resizing -------------------------------- */

    function getPanelWidth() {
        return els.codePanel.getBoundingClientRect().width || DEFAULT_PANEL_WIDTH;
    }

    function clampPanelWidth(width) {
        const max = Math.max(MIN_PANEL_WIDTH, els.viewerBody.clientWidth - MIN_STAGE_WIDTH);
        return Math.min(Math.max(width, MIN_PANEL_WIDTH), max);
    }

    function applyPanelWidth(width, persist = true) {
        const clamped = clampPanelWidth(width);
        els.codePanel.style.width = `${clamped}px`;
        els.panelResizer.setAttribute('aria-valuenow', String(Math.round(clamped)));
        if (persist) {
            try {
                localStorage.setItem(PANEL_WIDTH_KEY, String(Math.round(clamped)));
            } catch (e) { /* storage unavailable — ignore */ }
        }
        return clamped;
    }

    function restorePanelWidth() {
        let width = DEFAULT_PANEL_WIDTH;
        try {
            const saved = parseInt(localStorage.getItem(PANEL_WIDTH_KEY), 10);
            if (!Number.isNaN(saved)) {
                width = saved;
            }
        } catch (e) { /* storage unavailable — ignore */ }
        applyPanelWidth(width, false);
    }

    function setupPanelResizer() {
        let dragging = false;
        let startX = 0;
        let startWidth = 0;

        const onPointerDown = (event) => {
            if (els.codePanel.hidden) {
                return;
            }
            event.preventDefault();
            dragging = true;
            startX = event.clientX;
            startWidth = getPanelWidth();
            document.body.classList.add('resizing');
            els.panelResizer.setPointerCapture(event.pointerId);
        };

        const onPointerMove = (event) => {
            if (!dragging) {
                return;
            }
            applyPanelWidth(startWidth + (event.clientX - startX));
        };

        const onPointerUp = (event) => {
            if (!dragging) {
                return;
            }
            dragging = false;
            document.body.classList.remove('resizing');
            els.panelResizer.releasePointerCapture(event.pointerId);
        };

        const onKeydown = (event) => {
            if (els.codePanel.hidden) {
                return;
            }
            const step = event.shiftKey ? 64 : 16;
            switch (event.key) {
                case 'ArrowLeft':
                    applyPanelWidth(getPanelWidth() - step);
                    event.preventDefault();
                    break;
                case 'ArrowRight':
                    applyPanelWidth(getPanelWidth() + step);
                    event.preventDefault();
                    break;
                case 'Home':
                    applyPanelWidth(MIN_PANEL_WIDTH);
                    event.preventDefault();
                    break;
                case 'End':
                    applyPanelWidth(clampPanelWidth(Number.MAX_SAFE_INTEGER));
                    event.preventDefault();
                    break;
            }
        };

        els.panelResizer.addEventListener('pointerdown', onPointerDown);
        els.panelResizer.addEventListener('pointermove', onPointerMove);
        els.panelResizer.addEventListener('pointerup', onPointerUp);
        els.panelResizer.addEventListener('pointercancel', onPointerUp);
        els.panelResizer.addEventListener('dblclick', () => {
            applyPanelWidth(DEFAULT_PANEL_WIDTH);
        });
        els.panelResizer.addEventListener('keydown', onKeydown);

        window.addEventListener('resize', () => {
            if (!els.codePanel.hidden) {
                applyPanelWidth(getPanelWidth());
            }
        });
    }

    /* =========================================================================
     * Search overlay
     * ========================================================================= */

    function openSearch() {
        searchOpen = true;
        searchActiveIndex = -1;
        els.searchOverlay.hidden = false;
        els.searchInput.value = '';
        renderSearchResults('');
        els.searchInput.focus();
    }

    function closeSearch() {
        searchOpen = false;
        searchActiveIndex = -1;
        els.searchOverlay.hidden = true;
        els.searchResults.innerHTML = '';
    }

    function allDashboards() {
        const flat = [];
        samplesData.categories.forEach((category) => {
            category.dashboards.forEach((dashboard) => flat.push(dashboard));
        });
        return flat;
    }

    function renderSearchResults(term) {
        const filtered = term
            ? allDashboards().filter(
                (d) =>
                    d.name.toLowerCase().includes(term) ||
                    d.category.toLowerCase().includes(term)
            )
            : [];

        searchActiveIndex = filtered.length > 0 ? 0 : -1;
        els.searchResults.innerHTML = '';

        if (filtered.length === 0) {
            const empty = document.createElement('div');
            empty.className = 'search-empty';
            empty.textContent = term ? 'No dashboards found' : 'Type to search dashboards…';
            els.searchResults.appendChild(empty);
            return;
        }

        filtered.forEach((dashboard, index) => {
            const item = document.createElement('button');
            item.type = 'button';
            item.className = 'search-result' + (index === searchActiveIndex ? ' active' : '');
            item.dataset.index = String(index);
            item.dataset.path = dashboard.path;
            item.innerHTML = `
                <span class="search-result-name">${escapeHtml(dashboard.name)}</span>
                <span class="search-result-category">${escapeHtml(dashboard.category)}</span>
            `;
            item.addEventListener('click', () => openDashboard(dashboard));
            els.searchResults.appendChild(item);
        });
    }

    function setActiveSearchResult(index) {
        const items = els.searchResults.querySelectorAll('.search-result');
        items.forEach((item) => item.classList.remove('active'));
        if (index >= 0 && items[index]) {
            items[index].classList.add('active');
            items[index].scrollIntoView({ block: 'nearest' });
        }
        searchActiveIndex = index;
    }

    function openActiveSearchResult() {
        const active = els.searchResults.querySelector('.search-result.active');
        if (active) {
            const dashboard = findDashboard(null, active.dataset.path);
            if (dashboard) {
                openDashboard(dashboard);
            }
        }
        closeSearch();
    }

    function setupSearch() {
        els.searchOpen.addEventListener('click', openSearch);
        els.searchClose.addEventListener('click', closeSearch);
        els.searchOverlay.addEventListener('click', (event) => {
            if (event.target === els.searchOverlay) {
                closeSearch();
            }
        });

        els.searchInput.addEventListener('input', (event) => {
            renderSearchResults(event.target.value.trim().toLowerCase());
        });

        els.searchInput.addEventListener('keydown', (event) => {
            const items = els.searchResults.querySelectorAll('.search-result');
            if (event.key === 'ArrowDown') {
                event.preventDefault();
                setActiveSearchResult(Math.min(searchActiveIndex + 1, items.length - 1));
            } else if (event.key === 'ArrowUp') {
                event.preventDefault();
                setActiveSearchResult(Math.max(searchActiveIndex - 1, 0));
            } else if (event.key === 'Enter') {
                event.preventDefault();
                openActiveSearchResult();
            }
        });
    }

    /* =========================================================================
     * Theme
     * ========================================================================= */

    function applyTheme(theme, persist = true) {
        document.documentElement.dataset.theme = theme;
        els.themeToggle.title =
            theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme';
        if (persist) {
            try {
                localStorage.setItem(THEME_KEY, theme);
            } catch (e) { /* storage unavailable — ignore */ }
        }
    }

    function setupTheme() {
        let theme = 'dark';
        try {
            theme = localStorage.getItem(THEME_KEY) || 'dark';
        } catch (e) { /* storage unavailable — ignore */ }
        applyTheme(theme, false);

        els.themeToggle.addEventListener('click', () => {
            const next =
                document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
            applyTheme(next);
        });
    }

    /* =========================================================================
     * Global actions & keyboard shortcuts
     * ========================================================================= */

    function setupViewerActions() {
        els.backBtn.addEventListener('click', () => {
            if (window.location.hash) {
                window.location.hash = '';
            } else {
                showHome();
            }
        });

        els.sourceToggle.addEventListener('click', toggleCodePanel);
        els.closeCode.addEventListener('click', () => showCodePanel(false));
        els.copyYaml.addEventListener('click', copyYaml);
        els.copyCode.addEventListener('click', copyYaml);

        els.openNewWindow.addEventListener('click', () => {
            if (currentDashboard) {
                const url = `${window.location.origin}${window.location.pathname}#${currentDashboard.category}/${encodeURIComponent(currentDashboard.path)}`;
                window.open(url, '_blank');
            }
        });

        els.reloadDashboard.addEventListener('click', () => {
            if (currentDashboard) {
                loadDashboardInIframe(currentDashboard.path);
                loadDashboardSourceCode(currentDashboard);
            }
        });

        els.dashboardIframe.addEventListener('load', () => {
            els.stageLoading.classList.add('hidden');
        });

        // Global keyboard shortcuts
        document.addEventListener('keydown', (event) => {
            if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
                event.preventDefault();
                if (searchOpen) {
                    closeSearch();
                } else {
                    openSearch();
                }
                return;
            }
            if (event.key === 'Escape' && searchOpen) {
                closeSearch();
                return;
            }
            const target = document.activeElement;
            const inField =
                target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA');
            if ((event.ctrlKey || event.metaKey || event.altKey) || inField) {
                return;
            }
            if (
                (event.key === 's' || event.key === 'S') &&
                !els.viewerView.hidden
            ) {
                toggleCodePanel();
            }
        });
    }

    /* =========================================================================
     * Boot
     * ========================================================================= */

    function boot() {
        setupTheme();
        setupSearch();
        setupViewerActions();
        setupPanelResizer();
        window.addEventListener('hashchange', route);

        loadSamples().then(() => {
            route();
        });
    }

    boot();
})();
