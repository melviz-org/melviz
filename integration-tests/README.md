# Melviz Integration Tests

End-to-end smoke tests for the **final webapp deliverable**. They serve the
built examples gallery (`examples/dist/`) and load the **Kitchensink**
dashboard, then verify that the core Melviz functionalities are alive after a
full build:

- the webapp boots and the dashboard YAML actually loads
- an **internal chart** (ECharts displayer) draws into a `<canvas>`
- a **table** renders the dataset (PatternFly table)
- an **external component** (the `echarts` React component, embedded in an
  iframe) renders its own chart
- the webapp throws no fatal page-level JS errors

It is intentionally *not* exhaustive — it only guards that the built webapp is
functional, so a broken build is caught early.

## Prerequisites

1. A full build must have produced `examples/dist/`. From the repo root:

   ```bash
   yarn build:examples      # or: yarn build:prod
   ```

2. Install the Playwright browser (first time only):

   ```bash
   cd integration-tests
   npx playwright install chromium
   ```

## Run

```bash
cd integration-tests
npx playwright test
```

The config starts a static server over `examples/dist` on port `8080`
(override with `MELVIZ_IT_PORT=...`). If a server is already running on that
port (e.g. `yarn workspace @melviz/examples serve`), it is reused.

## Layout

- `playwright.config.js` — server + test setup
- `tests/kitchensink.spec.js` — the smoke test
