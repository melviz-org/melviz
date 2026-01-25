Melviz
--

[![Java CI](https://github.com/jesuino/melviz/actions/workflows/ci-java.yml/badge.svg)](https://github.com/jesuino/melviz/actions/workflows/ci-java.yml)
[![JavaScript CI](https://github.com/jesuino/melviz/actions/workflows/ci-javascript.yml/badge.svg)](https://github.com/jesuino/melviz/actions/workflows/ci-javascript.yml)
[![CodeQL](https://github.com/jesuino/melviz/actions/workflows/codeql.yml/badge.svg)](https://github.com/jesuino/melviz/actions/workflows/codeql.yml)
[![Build and Publish](https://github.com/jesuino/melviz/actions/workflows/build-publish-webapp.yml/badge.svg)](https://github.com/jesuino/melviz/actions/workflows/build-publish-webapp.yml)

Melviz is a tool to create data visualizations, dashboards and reports built using YAML.

* Supports YAML based pages, allowing users to build dahsboards and reports in a declarative way;
* Can read data from JSON, metrics and CSV sources;
* Data can be transformed using JSONAta;
* Support microfrontends for custom visualizations;
* Can pull real-time data from its datasets;
* Allow Communication between components using Filter components;

Licensed under the Apache License, Version 2.0

For further information, please visit the project web site <a href="http://melviz.org" target="_blank">melviz.org</a>

This is the mono repo for all Melviz Javascript API, Components and Web Apps. Here's a brief description of each directory:

* **core**: Core web app with HTML bundle
* **packages**: Contains base APIs for building Melviz applications and components;
* **components**: Micro frontends for visualizing data from Melviz
* **webapp**: Melviz web app distribution. It can be embed in other applications or run as a standalone app.

## Building Melviz

Melviz is a hybrid Java/Maven + JavaScript/Yarn monorepo. Build processes are separated by subsystem.

### Prerequisites

* Java 17 or higher
* Maven 3.6+
* Node.js 16+
* Yarn 4.10.3 (included via Yarn Berry)

### Quick Start - Full Build

```bash
# 1. Install JavaScript dependencies
yarn install

# 3. Build all JavaScript components and webapp
yarn workspaces foreach -A run build
```

The final application will be in [webapp/dist/](webapp/dist/).

### Java Core (GWT-based webapp)

```bash
# Go into core App
cd core

# Build all Java modules
yarn build

# Run tests only
yarn test

# Production build
yarn build:prod
```

The compiled web application will be in [core/melviz-webapp-parent/melviz-webapp/target/melviz-webapp/](core/melviz-webapp-parent/melviz-webapp/target/melviz-webapp/).

### JavaScript Components and Webapp

```bash
# Build all workspace packages (from repository root)
# The -t flag ensures packages are built in topological order (dependencies first)
yarn workspaces foreach -At run build

# Build a specific component
cd components/melviz-component-echarts
yarn build

# Build final webapp (assembles all components)
cd webapp
yarn build
```

### Development Mode

Run a component in development mode with hot reload:

```bash
cd components/melviz-component-echarts
yarn start  # Starts webpack-dev-server on port 9001
```

### Testing

To run test on all packages

JavaScript component tests:
```bash
yarn workspaces foreach -A run test
```

### Production Build

This is a slower command, but produces a production-ready package in `webapp/dist` dir:

```bash
yarn install
yarn workspaces foreach -A run build:prod
```

## Architecture Overview

### Monorepo Structure

Melviz is organized as a monorepo with Yarn workspaces:

- **`core/`** - Java/Maven-based backend using GWT (Google Web Toolkit) to compile Java to JavaScript
- **`packages/`** - Shared TypeScript libraries and build tooling
- **`components/`** - Independent React-based microfrontend visualization components
- **`webapp/`** - Webpack orchestrator that assembles the final application

### Hybrid Build System

The build system combines two ecosystems:

1. **Java/GWT Side**: Maven builds Java code in `core/`, compiling it to JavaScript via GWT. Uses Java 17.
2. **JavaScript Side**: Yarn workspaces manage TypeScript/React components, shared packages, and final webapp assembly.
3. **Integration**: The `webapp/` webpack build copies the GWT-compiled core and all component bundles into a unified `dist/` directory.

### Microfrontend Component Architecture

Each component in `components/` is a self-contained React microfrontend that communicates with the core via the Component API.

**Component Lifecycle Pattern**:
```typescript
// 1. Component gets controller from ComponentApi
const api = new ComponentApi();
const controller = api.getComponentController();

// 2. Register dataset handler
controller.setOnDataSet((dataset, params) => {
  // Transform dataset and update visualization
});

// 3. Register initialization handler
controller.setOnInit((params) => {
  // Initialize with configuration
});

// 4. Signal ready
controller.ready();

// 5. Send filters back to core
controller.filter(filterRequest);
```

**Key Interface (`@melviz/component-api`)**:
- `ComponentController` - Manages component lifecycle and communication
- `ComponentBus` - Message bus for inter-component communication
- `DataSet` - Data structure passed from core to components
- `FilterRequest` - Filter queries sent from components back to core
- `FunctionCallRequest` - Backend function calls

### Data Flow

```
YAML Definition → Java Core (GWT) → Dataset Processing → Component API → React Components
                                        ↑                                    ↓
                                        └────────── Filters/Events ──────────┘
```

1. **Java Core** (`core/melviz-webapp-shared`, `core/melviz-dataset/`, etc.):
   - Parses YAML dashboard definitions
   - Loads data from JSON/CSV/metrics sources
   - Applies JSONata transformations
   - Manages filter state
   - Compiled to JavaScript via GWT

2. **Component API** (`packages/melviz-component-api`):
   - TypeScript bridge between GWT core and React components
   - Uses message bus pattern for async communication
   - Type-safe interfaces ensure contract compliance

3. **React Components** (`components/*/`):
   - Pure presentation/visualization logic
   - Receive datasets via `setOnDataSet` callback
   - Send filters back via `controller.filter()`
   - Independently bundled and deployed

### Module Organization

**Core Java Modules** (`core/`):
- `melviz-base/` - Foundational modules (dataset, JSON handling)
- `melviz-shared/` - Shared API contracts (displayer, navigation, services)
- `melviz-client/` - GWT-compilable client code (UI components, editors, renderers)
- `melviz-webapp-parent/melviz-webapp/` - Main web application assembly (produces WAR)

**Shared Packages** (`packages/`):
- `melviz-component-api` - Component controller and communication interfaces
- `melviz-component-dev` - Development utilities for component testing
- `webpack-base` - Common webpack configuration with TypeScript loader
- `tsconfig` - Shared TypeScript configuration

**Available Components** (`components/`):
- `melviz-component-echarts` - Apache ECharts visualizations
- `melviz-component-echarts-base` - Reusable ECharts wrapper
- `melviz-component-llm-prompter` - LLM prompt engineering UI
- `melviz-component-map` - Geographic map visualizations
- `melviz-component-svg-heatmap` - SVG-based heatmaps

### Adding a New Component

1. Create new directory in `components/melviz-component-<name>/`
2. Add `package.json` with dependency on `@melviz/component-api`
3. Create `src/index.tsx` with ComponentController integration
4. Add webpack configuration (can extend `webpack-base`)
5. Register component in `webapp/package.json` devDependencies
6. Update `webapp/webpack.config.js` to copy component bundle
7. Build with `yarn build` - output goes to `dist/index.js`

### Deployment

The final artifact is a single directory (`webapp/dist/`) containing:
- GWT-compiled Java core (from `core/melviz-webapp/target/`)
- All component bundles (from `components/*/dist/`)
- Static assets and HTML entry points

This can be deployed to any static web server or GitHub Pages.

## Working with YAML Dashboards

Melviz renders dashboards defined in YAML. The application can receive content dynamically via `postMessage`:

```javascript
window.postMessage(`pages:
  - components:
    - markdown: "# Hello World!"
`, null)
```

Alternatively, use `setup.js` to configure static dashboards that load on startup.

## Key Technologies

- **Java**: JDK 17
- **Maven**: Build orchestration for Java modules
- **GWT (Google Web Toolkit)**: Compiles Java to JavaScript for client-side execution
- **Yarn**: v4.10.3 for workspace management
- **TypeScript**: 4.6.2 for type-safe component development
- **React**: 17.0.2 for component UI
- **Webpack**: 5.x for module bundling
- **Jest**: Testing framework with ts-jest for TypeScript
- **Apache ECharts**: Visualization library used by echarts component
- **Patternfly**: React Components Package
- **JSONata**: Data transformation language for dataset processing
