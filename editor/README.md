# Melviz Editor

A browser-based **YAML dashboard editor** with a live preview powered by the
`@melviz/webapp` package.

Edit a Melviz dashboard on the left, watch it render on the right — the preview
refreshes automatically ~500ms after you stop typing.

![layout](https://img.shields.io/badge/CodeMirror-6-blue)

## Features

- **CodeMirror 6** YAML editor (syntax highlighting, search, line wrapping,
  `Mod-s` to save).
- **Live preview**: the dashboard is rendered in an embedded `@melviz/webapp`
  iframe that reloads on a 500ms debounce after each edit.
- **File management**: New / Open / Download, plus **Save** to browser
  `localStorage` (your file and recent-files history survive a reload).
- **GitHub import**: point the editor at any public (or token-authenticated
  private) GitHub repository, browse its YAML files, and load one into the
  editor.
- **Theme toggle** (dark/light) and a **draggable split** between editor and
  preview.

## How it works

The editor is a self-contained static single-page app. It embeds the
`@melviz/webapp` package (built into `webapp/dist`) in an `<iframe>`. The
iframe's `src` uses the webapp's `?import=<path>` parameter so it loads a
dashboard model from a URL.

Because the editor must feed the *current, in-progress* YAML to the preview
while it is still being edited, the editor's server exposes a **virtual
dashboard endpoint**:

- `POST /_editor/dashboards` — the editor registers the current document
  (`{ name, yaml, fileName }`).
- `GET /dashboards/editor/<name>.dash.yaml` — the webapp iframe fetches this
  URL (resolved relative to `melviz-webapp/` as `../dashboards/editor/<name>.dash.yaml`).

Every time the editor content changes, it re-registers the document (so the
endpoint always serves the latest text) and reloads the iframe. The webapp's
`RuntimeClientLoader` then resolves the `import` parameter against the virtual
endpoint and renders the result.

## Building

Build the `@melviz/webapp` package first (the editor bundles it), then build
the editor:

```bash
# from the repository root
yarn build:webapp     # produces webapp/dist (required)
yarn build:editor     # bundles CodeMirror + copies the webapp + starter dashboards
```

Or build the webapp and editor together:

```bash
yarn build:webapp && yarn build:editor
```

`yarn build:editor` produces `editor/dist/`:

```
dist/
├── index.html          # editor UI (split panel)
├── app.js              # editor logic (classic script)
├── styles.css          # themes + layout
├── setup.js            # injected webapp configuration (CLIENT mode)
├── codemirror.js       # CodeMirror 6 bundled by esbuild (exposes window.CodeMirror6)
├── dashboards/         # starter dashboards
└── melviz-webapp/      # the @melviz/webapp package (copied from webapp/dist)
```

## Running

Serve the built editor (the server also hosts the virtual dashboard endpoint):

```bash
# from the repository root
yarn serve:editor          # http://localhost:8080
```

or from the `editor/` workspace:

```bash
yarn serve                 # http://localhost:8080 (override with PORT=...)
```

Open <http://localhost:8080> in a browser.

### Development

A dev server serves the editor **source** (`editor/src/`) live, the webapp from
`webapp/dist/`, and the virtual dashboard endpoint, so you can edit
`editor/src/*` and refresh the browser to see changes without re-running the
copy/build steps:

```bash
yarn dev:editor            # http://localhost:8080
```

> Re-run `yarn dev:editor` (or `yarn build:editor`) after changes to
> `webapp/dist` (i.e. after `yarn build:webapp`) — the webapp bundle is
> re-copied/linked at server start.

## Notes & limitations

- **Live preview = iframe reload.** Melviz has no in-place "replace current
  model" hook exposed to the host page, so real-time editing is implemented as
  a debounced reload of the embedded webapp iframe. This keeps the preview
  accurate at the cost of a full re-render every ~500ms.
- **CodeMirror is bundled, not inlined.** `codemirror.js` is an esbuild IIFE
  bundle exposing `window.CodeMirror6 = { EditorState, EditorView, yaml, keymap,
  searchKeymap }`. The editor loads it as a plain `<script>` before `app.js`.
- **Headless-browser quirk.** In some headless Chromium environments
  (e.g. Playwright), `new EditorView(...)` can throw a `scrollTo`-related error
  during DOM mount. This is an environment artifact, not a code defect — the
  editor detects the failure and **falls back to a plain `<textarea>`** so
  editing and live preview still work. In a normal desktop browser CodeMirror
  mounts successfully.
- **GitHub token** is optional; public repositories work without one. The token
  is stored in `localStorage` and sent only to `api.github.com`.
