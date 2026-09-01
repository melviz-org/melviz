const fs = require('fs');
const path = require('path');

// Copies the built @melviz/webapp (webapp/dist) into editor/dist/melviz-webapp
// so the editor can render dashboards with the full Melviz webapp — never the
// raw core webapp. The editor shell itself lives at the dist root.
const sourceDir = path.join(__dirname, '../../webapp/dist');
const targetDir = path.join(__dirname, '../dist/melviz-webapp');

function copyRecursive(src, dest) {
  if (!fs.existsSync(src)) {
    console.error(`Source directory not found: ${src}`);
    console.error('Please build the melviz webapp first by running: yarn build:webapp');
    process.exit(1);
  }
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

if (!fs.existsSync(targetDir)) {
  fs.mkdirSync(targetDir, { recursive: true });
}

// Copy the webapp
console.log('Copying @melviz/webapp from:', sourceDir);
copyRecursive(sourceDir, targetDir);

// The webapp reads its configuration from setup.js BEFORE the GWT runtime
// boots. We ship the editor-specific setup so that:
//   - mode CLIENT loads the current dashboard via the `import` param
//   - samplesUrl points at /dashboards, which the editor's server keeps in
//     sync with the YAML being edited (see dev-server.js / serve-melviz.js)
const setupSrc = path.join(__dirname, '../src/setup.js');
if (fs.existsSync(setupSrc)) {
  fs.copyFileSync(setupSrc, path.join(targetDir, 'setup.js'));
  console.log('setup.js copied from src to melviz-webapp');
} else {
  console.warn('Warning: setup.js not found in src directory');
}

console.log('Melviz webapp copied successfully');
