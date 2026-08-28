/* Melviz webapp setup — used by the examples gallery.
 *
 * The `import` query parameter points the webapp at the dashboard YAML being
 * showcased. The mode CLIENT ensures a single model is loaded straight from
 * that URL. The default theme follows the gallery theme persisted in
 * localStorage (dark by default) so embedded dashboards match the gallery.
 */
(function () {
    var theme = 'dark';
    try {
        theme = localStorage.getItem('melviz.examples.theme') || 'dark';
    } catch (e) {
        /* storage unavailable — keep default */
    }
    melviz = {
        allowExternal: true,
        mode: 'CLIENT',
        settings: {
            mode: theme === 'dark' ? 'DARK' : 'LIGHT'
        }
    };
})();
