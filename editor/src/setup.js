/* Melviz webapp setup — used by the Melviz Editor.
 *
 * The editor renders dashboards with the @melviz/webapp package:
 * the `import` query parameter points the webapp at the current dashboard
 * YAML. In CLIENT mode a single model is loaded straight from that URL.
 *
 * The default theme follows the editor theme persisted in localStorage
 * (dark by default) so the embedded webapp matches the editor chrome.
 */
(function () {
    var theme = 'dark';
    try {
        theme = localStorage.getItem('melviz.editor.theme') || 'dark';
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
