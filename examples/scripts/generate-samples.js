const fs = require('fs');
const path = require('path');

const dashboardsDir = path.join(__dirname, '../dashboards');
const outputFile = path.join(__dirname, '../samples.json');

// Short descriptions for known categories, used by the gallery UI.
// Dashboards placed directly in the dashboards/ root are listed under "General".
const CATEGORY_DESCRIPTIONS = {
  General: 'Standalone dashboards: live data sources and quick-start samples.',
  'Basic Usage': 'Fundamental building blocks: datasets, layouts, columns, filters and more.',
  Dashboards: 'Complete reference dashboards combining many Melviz features.',
  'Prometheus Metrics': 'Dashboards wired to Prometheus-compatible metrics: JVM, Quarkus, JupyterHub, Kepler, Ansible, Backstage and more.',
  'ML Serving': 'Model serving metrics from the ModelMesh and Triton Inference Servers.',
  'Traces & Telemetry': 'Distributed traces rendered from OpenTelemetry data.'
};

// Recognized dashboard file extensions (case-insensitive).
const DASHBOARD_EXTENSIONS = ['dash.yaml', 'dash.yml', 'yaml', 'yml'];

function isDashboardFile(file) {
  const lower = file.toLowerCase();
  return DASHBOARD_EXTENSIONS.some(ext => lower.endsWith('.' + ext));
}

function stripExtension(file) {
  return file.replace(new RegExp('\\.(' + DASHBOARD_EXTENSIONS.join('|') + ')$', 'i'), '');
}

// Recursively find all dashboard files
function findDashboards(dir, baseDir = dir) {
  const files = fs.readdirSync(dir);
  const dashboards = [];

  for (const file of files) {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);

    if (stat.isDirectory()) {
      dashboards.push(...findDashboards(filePath, baseDir));
    } else if (isDashboardFile(file)) {
      const relativePath = path.relative(baseDir, filePath);
      const category = path.dirname(relativePath).split(path.sep)[0];

      dashboards.push({
        name: stripExtension(file),
        path: relativePath.replace(/\\/g, '/'), // Normalize path separators
        category: category === '.' ? 'General' : category,
        file: file
      });
    }
  }

  return dashboards;
}

// Generate the samples.json
const dashboards = findDashboards(dashboardsDir);

// Group by category
const categories = {};
dashboards.forEach(dashboard => {
  if (!categories[dashboard.category]) {
    categories[dashboard.category] = [];
  }
  categories[dashboard.category].push(dashboard);
});

// Sort categories and dashboards
const sortedCategories = Object.keys(categories).sort();
const samples = sortedCategories.map(category => ({
  category: category,
  description: CATEGORY_DESCRIPTIONS[category] || '',
  dashboards: categories[category].sort((a, b) => a.name.localeCompare(b.name))
}));

const output = {
  version: '1.0.0',
  description: 'Melviz Dashboard Examples',
  totalDashboards: dashboards.length,
  categories: samples
};

fs.writeFileSync(outputFile, JSON.stringify(output, null, 2));
console.log(`Generated samples.json with ${dashboards.length} dashboards in ${sortedCategories.length} categories`);
