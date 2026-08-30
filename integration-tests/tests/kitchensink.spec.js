// Integration / smoke test for the final Melviz webapp deliverable.
//
// What it does:
//   - starts a static server over `examples/dist` (requires a prior full build:
//     `yarn build:examples` / `yarn build:prod`)
//   - loads the Kitchensink dashboard through the webapp entry point
//   - verifies the most important basic functionalities are alive after a
//     full build: dashboard boots, navigation works, an internal chart renders,
//     a table renders, and an external component renders.
//
// It intentionally does NOT assert on every page/feature — just that the built
// webapp is functional.
//
// Run:
//   cd integration-tests && npx playwright install chromium && npx playwright test

const { test, expect } = require('@playwright/test');

// The examples gallery serves the webapp from /melviz-webapp and the dashboard
// YAML from /dashboards. The webapp resolves the `import` URL relative to the
// page, so we point at the YAML with a relative path (../dashboards/...).
const DASHBOARD_URL =
    '/melviz-webapp/?import=' +
    encodeURIComponent('../dashboards/Basic Usage/Kitchensink.dash.yml');

// Returns true if the first <canvas> on the given page/frame contains drawn
// pixels (i.e. the chart actually rendered, not just a blank canvas).
async function canvasHasContent(frame) {
    return frame.evaluate(() => {
        const c = document.querySelector('canvas');
        if (!c) return false;
        const ctx = c.getContext('2d');
        if (!ctx) return false;
        const data = ctx.getImageData(0, 0, c.width, c.height).data;
        for (let i = 3; i < data.length; i += 4) {
            if (data[i] !== 0) return true;
        }
        return false;
    });
}

test.describe('Melviz webapp — Kitchensink dashboard', () => {
    test('renders charts, tables and external components', async ({ page }) => {
        test.setTimeout(180 * 1000);

        const pageErrors = [];
        page.on('pageerror', (e) => pageErrors.push(String(e)));

        await page.goto(DASHBOARD_URL);

        // 1. Webapp boots and the dashboard actually loads. The "Melviz
        //    Components" HTML banner is rendered from the Kitchensink YAML, so
        //    waiting for it is a reliable "dashboard is up" signal (the GWT
        //    loading spinner alone is hidden even on a failed load).
        await expect(page.locator('body > #app')).not.toBeEmpty();
        await expect(page.getByText('Melviz Components').first()).toBeVisible({
            timeout: 90 * 1000
        });

        // 2. Top-level navigation tabs are present.
        const navTab = (name) =>
            page
                .locator('.pf-v5-c-tabs__item', { hasText: name })
                .first();
        await expect(navTab('Displayers')).toBeVisible();

        const goToPage = async (name) => {
            await navTab(name).click();
            await page.waitForTimeout(1500);
        };

        // 3. Internal chart: the ECharts displayer draws into a <canvas>.
        await goToPage('Bar Chart');
        const chartCanvas = page.locator('canvas').first();
        await expect(chartCanvas).toBeVisible();
        await expect
            .poll(() => canvasHasContent(page), { timeout: 30 * 1000 })
            .toBeTruthy();

        // 4. Table: the products dataset is rendered as a PatternFly table.
        await goToPage('Table');
        const table = page.locator('table').first();
        await expect(table).toBeVisible();
        await expect(table.locator('th', { hasText: 'Product' })).toBeVisible();
        await expect(page.locator('td', { hasText: 'Scanner' })).toBeVisible();

        // 5. External component: the echarts React component is embedded in an
        //    iframe and renders its own chart (canvas with drawn pixels).
        await goToPage('External Components');
        await navTab('ECharts').click();
        const echartsFrame = page.frameLocator(
            'iframe[src*="component/echarts"]'
        );
        const echartsCanvas = echartsFrame.locator('canvas');
        await expect(echartsCanvas).toBeVisible({ timeout: 30 * 1000 });
        await expect
            .poll(async () =>
                canvasHasContent(
                    page.frames().find((f) => f.url().includes('component/echarts'))
                )
            , { timeout: 30 * 1000 })
            .toBeTruthy();

        // 6. The webapp itself must not have thrown fatal JS errors.
        expect(
            pageErrors,
            `webapp page errors: ${pageErrors.join(' | ')}`
        ).toHaveLength(0);
    });
});
