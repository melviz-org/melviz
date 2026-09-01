const fs = require('fs');
const path = require('path');

// Copies the bundled starter dashboards into dist/dashboards so the editor
// can fetch them (and the user can pick one to edit) via the samples URL.
const sourceDir = path.join(__dirname, '../dashboards');
const targetDir = path.join(__dirname, '../dist/dashboards');

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
  console.warn('No dashboards directory found, skipping');
  process.exit(0);
}

console.log('Copying starter dashboards...');
copyRecursive(sourceDir, targetDir);
