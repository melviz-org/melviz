// Integration tests for the final Melviz webapp deliverable.
//
// Prerequisite: a full build must have been run (see AGENTS.md):
//   yarn build:examples   (or `yarn build:prod`)
// so that `examples/dist/` exists.
//
// Run:
//   cd integration-tests
//   npx playwright install chromium   # first time only
//   npx playwright test
const path = require('path');

const PORT = Number(process.env.MELVIZ_IT_PORT || 8080);

module.exports = {
    testDir: './tests',
    // The GWT core + components are heavy; give plenty of room.
    timeout: 120 * 1000,
    expect: { timeout: 30 * 1000 },
    reporter: [['list']],
    use: {
        baseURL: `http://127.0.0.1:${PORT}`,
        headless: true,
        viewport: { width: 1600, height: 1000 },
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure'
    },
    // Serve the built examples gallery (webapp + dashboards) on a fixed port.
    // The test itself does the rest through Playwright.
    webServer: {
        command: `npx http-server "${path.join(__dirname, '../examples/dist')}" -p ${PORT} -s`,
        url: `http://127.0.0.1:${PORT}`,
        reuseExistingServer: true,
        timeout: 60 * 1000
    }
};
