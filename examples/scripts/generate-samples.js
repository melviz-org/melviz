const fs = require('fs');
const path = require('path');

const dashboardsDir = path.join(__dirname, '../dashboards');
const outputFile = path.join(__dirname, '../samples.json');

// Recursively find all dashboard files
function findDashboards(dir, baseDir = dir) {
  const files = fs.readdirSync(dir);
  const dashboards = [];

  for (const file of files) {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);

    if (stat.isDirectory()) {
      dashboards.push(...findDashboards(filePath, baseDir));
    } else if (file.endsWith('.dash.yaml') || file.endsWith('.dash.yml') || file.endsWith('.yml')) {
      const relativePath = path.relative(baseDir, filePath);
      const name = file.replace(/\.(dash\.yaml|dash\.yml|yml)$/, '');
      const category = path.dirname(relativePath).split(path.sep)[0];

      dashboards.push({
        name: name,
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
