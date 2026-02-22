#!/usr/bin/env node

/**
 * Development server with hot reload for dashboard YAML files
 *
 * This script:
 * 1. Watches dashboard YAML files for changes
 * 2. Rebuilds samples.json and copies dashboards on change
 * 3. Auto-reloads the browser using BrowserSync
 */

const chokidar = require('chokidar');
const browserSync = require('browser-sync').create();
const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

const DASHBOARDS_DIR = path.join(__dirname, '../dashboards');
const DIST_DIR = path.join(__dirname, '../dist');
const WATCH_PATTERNS = [
  path.join(DASHBOARDS_DIR, '**/*.dash.yaml'),
  path.join(DASHBOARDS_DIR, '**/*.dash.yml'),
  path.join(DASHBOARDS_DIR, '**/*.yml'),
  path.join(DASHBOARDS_DIR, '**/*.yaml')
];

console.log('🚀 Starting Melviz Dashboard Development Server...\n');

// Ensure dist directory exists
if (!fs.existsSync(DIST_DIR)) {
  console.log('📦 Running initial build...');
  execSync('npm run build', { stdio: 'inherit' });
}

// Function to rebuild dashboards
function rebuild() {
  console.log('🔄 Dashboard changed, rebuilding...');
  const startTime = Date.now();

  try {
    // Regenerate samples.json and copy dashboards
    execSync('npm run generate-samples', { stdio: 'pipe' });
    execSync('npm run copy-dashboards', { stdio: 'pipe' });

    const duration = Date.now() - startTime;
    console.log(`✅ Rebuild complete (${duration}ms)`);

    // Reload browser
    browserSync.reload();
  } catch (error) {
    console.error('❌ Rebuild failed:', error.message);
  }
}

// Initialize BrowserSync
browserSync.init({
  server: {
    baseDir: DIST_DIR
  },
  port: 8080,
  ui: {
    port: 8081
  },
  open: true,
  notify: true,
  logLevel: 'info',
  logPrefix: 'Melviz',
  files: [
    // Watch dist directory for changes (from rebuilds)
    path.join(DIST_DIR, '**/*.html'),
    path.join(DIST_DIR, '**/*.js'),
    path.join(DIST_DIR, '**/*.css'),
    path.join(DIST_DIR, 'samples.json')
  ]
}, () => {
  console.log('\n✨ Development server ready!');
  console.log(`   Local: http://localhost:8080`);
  console.log(`   UI: http://localhost:8081`);
  console.log(`\n👀 Watching dashboard files in: ${DASHBOARDS_DIR}`);
  console.log('   Edit any .dash.yaml file to see changes instantly!\n');
});

// Watch dashboard files
const watcher = chokidar.watch(WATCH_PATTERNS, {
  ignored: /(^|[\/\\])\../,
  persistent: true,
  ignoreInitial: true
});

watcher
  .on('add', (filePath) => {
    console.log(`📄 Dashboard added: ${path.relative(DASHBOARDS_DIR, filePath)}`);
    rebuild();
  })
  .on('change', (filePath) => {
    console.log(`📝 Dashboard modified: ${path.relative(DASHBOARDS_DIR, filePath)}`);
    rebuild();
  })
  .on('unlink', (filePath) => {
    console.log(`🗑️  Dashboard deleted: ${path.relative(DASHBOARDS_DIR, filePath)}`);
    rebuild();
  })
  .on('error', (error) => {
    console.error('❌ Watcher error:', error);
  });

// Handle process termination
process.on('SIGINT', () => {
  console.log('\n👋 Shutting down development server...');
  watcher.close();
  browserSync.exit();
  process.exit(0);
});
