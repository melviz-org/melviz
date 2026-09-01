const fs = require('fs');
const path = require('path');

// Copies the editor source (src/) into dist/. The CodeMirror bundle
// (codemirror.js + codemirror.css) is generated separately by
// `build-codemirror.js` and lives in dist/.
const sourceDir = path.join(__dirname, '../src');
const targetDir = path.join(__dirname, '../dist');

function copyRecursive(src, dest) {
  const stat = fs.statSync(src);
  if (stat.isDirectory()) {
    fs.mkdirSync(dest, { recursive: true });
    for (const file of fs.readdirSync(src)) {
      copyRecursive(path.join(src, file), path.join(dest, file));
    }
  } else {
    fs.copyFileSync(src, dest);
  }
}

if (!fs.existsSync(sourceDir)) {
  console.error(`Editor source directory not found: ${sourceDir}`);
  process.exit(1);
}

fs.mkdirSync(targetDir, { recursive: true });
console.log('Copying editor source...');
copyRecursive(sourceDir, targetDir);
