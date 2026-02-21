# Melviz Examples Gallery

A web application that showcases all Melviz dashboard examples in an interactive gallery.

## Features

- Browse all dashboard examples organized by category
- Search dashboards by name or category
- View dashboards in an embedded Melviz viewer
- Open dashboards in new windows
- Reload dashboards on demand
- Responsive design with collapsible categories

## Prerequisites

Before building the examples gallery, you need to build the main Melviz webapp:

```bash
# From the repository root
yarn install
yarn build
```

This will create the compiled Melviz webapp in `webapp/dist/`.

## Building the Examples Gallery

```bash
# Install dependencies
npm install

# Build the gallery
npm run build
```

This will:
1. Generate `samples.json` with metadata about all dashboards
2. Copy the built Melviz webapp from `../webapp/dist/`
3. Copy all dashboard files and supporting data
4. Copy the HTML/CSS/JS files for the gallery interface

The final output will be in the `dist/` directory.

## Running the Gallery

After building, you can serve the gallery locally:

```bash
npm run serve
```

This will start a local web server at http://localhost:8080 and open it in your browser.

## Development

The gallery consists of:

- **`src/index.html`** - Main HTML structure
- **`src/styles.css`** - Styling for the gallery interface
- **`src/app.js`** - JavaScript application logic
- **`scripts/`** - Build scripts
  - `generate-samples.js` - Scans dashboards and creates samples.json
  - `copy-melviz.js` - Copies the built Melviz webapp
  - `copy-dashboards.js` - Copies dashboards and supporting files

## Project Structure

```
examples/
├── dashboards/          # Dashboard YAML files organized by category
├── src/                 # Source files for the gallery
│   ├── index.html
│   ├── styles.css
│   └── app.js
├── scripts/             # Build scripts
├── dist/                # Built gallery (generated)
├── samples.json         # Dashboard metadata (generated)
├── package.json
└── README.md
```

## Adding New Dashboards

Simply add new `.dash.yaml` or `.yml` files to the `dashboards/` directory. They will be automatically discovered when you run `npm run build`.

The file structure in `dashboards/` will determine the category organization in the gallery.

## License

Apache-2.0
