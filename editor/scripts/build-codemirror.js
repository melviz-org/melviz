// Builds the CodeMirror 6 bundle (dist/codemirror.js + codemirror.css) using
// esbuild. The bundle exposes `window.CodeMirror6` with the API the editor
// needs (EditorState, EditorView, yaml, keymap, searchKeymap).
const esbuild = require('esbuild');
const fs = require('fs');
const path = require('path');

const entry = path.join(__dirname, 'codemirror-entry.js');
const outDir = path.join(__dirname, '../dist');
if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
}

const options = {
    entryPoints: [entry],
    bundle: true,
    format: 'iife',
    globalName: 'CodeMirrorBundle',
    outfile: path.join(outDir, 'codemirror.js'),
    minify: true,
    target: ['es2020'],
    legalComments: 'none',
    logLevel: 'silent',
};

esbuild.build(options)
    .then(() => {
        console.log('CodeMirror bundle written to dist/codemirror.js');
    })
    .catch((err) => {
        console.error('esbuild build failed:', err);
        process.exit(1);
    });
