// CodeMirror 6 bundle entry point.
//
// Bundled to a single UMD file (dist/codemirror.js) by scripts/build-codemirror.js.
// The bundle exposes `window.CodeMirror6` with the API the editor uses.
import { EditorState } from '@codemirror/state';
import { EditorView, keymap } from '@codemirror/view';
import { yaml } from '@codemirror/lang-yaml';
import { searchKeymap } from '@codemirror/search';
import { EditorView as EV } from '@codemirror/view';

// Re-export the pieces the editor needs as a single namespace.
const CodeMirror6 = {
    EditorState,
    EditorView: EV,
    yaml,
    keymap,
    searchKeymap,
};

// UMD detection is handled by esbuild; assign to global so the classic-script
// loader in index.html can pick it up.
window.CodeMirror6 = CodeMirror6;

export default CodeMirror6;
