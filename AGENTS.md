# AGENTS.md

This file provides guidance to Agents when working with code in this repository.

## Build Commands

This is a hybrid Java/Maven + JavaScript/Yarn monorepo. Build processes are separated by subsystem.

### Java Core (GWT-based webapp)
```bash
# Build all Java modules (from ./core directory)
cd core && mvn clean install

# Build without tests
cd core && mvn clean install -DskipTests

# Run tests
cd core && mvn test
```

`core/` is also a Yarn workspace (`@melviz/core`) that wraps the same Maven builds:
- `yarn workspace @melviz/core run build` — `mvn clean install -DskipTests -Psources`
- `yarn workspace @melviz/core run build:prod` — `mvn clean install -Dfull` (production GWT compilation)
- `yarn workspace @melviz/core run test` — `mvn verify`

The compiled web application will be in `core/melviz-webapp-parent/melviz-webapp/target/melviz-webapp/`.

### JavaScript Components and Webapp

**Complete Build (Recommended for Clean Environments)**:
```bash
# From repository root - installs dependencies and builds everything in correct order
yarn install
yarn build
```

This handles the standard build:
1. Builds shared packages (`@melviz/component-api`, `@melviz/component-echarts-base`, `@melviz/component-dev`)
2. Builds Java core with Maven (`-DskipTests -Psources`)
3. Builds all React components
4. Assembles final webapp bundle

**Production Build**:
```bash
yarn build:prod
```
Same as `yarn build`, but builds the Java core in production mode (`mvn clean install -Dfull`) and also builds the examples gallery (`yarn build:examples`).

**Individual Build Commands**:
```bash
# Build only shared packages
yarn build:packages

# Build only Java core
yarn build:core

# Build only Java core in production mode
yarn build:core:prod

# Build only React components (requires packages to be built first)
yarn build:components

# Build only final webapp (requires everything else to be built first)
yarn build:webapp

# Build only the examples gallery (requires the webapp to be built first)
yarn build:examples

# Clean all workspaces and the Maven target directories
yarn clean
```

Build specific component:
```bash
cd components/melviz-component-echarts
yarn build  # Runs tests, cleans dist, then webpack
```

### Development Mode

Run a component in dev mode with hot reload:
```bash
cd components/melviz-component-echarts
yarn start  # Starts the webpack dev server on port 9001
```

Each component ships a `dev-webapp/` directory (`index.tsx`, `manifest.dev.json`, `webpack.config.js`) that renders the component standalone via `@melviz/component-dev` (`new ComponentDev().start()`), so it can be developed and tested without the Java core.

### Examples Gallery

The `examples/` workspace (`@melviz/examples`) is an interactive gallery that showcases the dashboard examples in `examples/dashboards/`. It requires the webapp to be built first (`yarn build` or `yarn build:prod` from the repository root).

```bash
cd examples
yarn dev    # browser-sync dev server on http://localhost:8080 (control UI on :8081), watches dashboards/ and live-reloads
yarn build  # generates samples.json, copies webapp/dist and all dashboards into dist/
yarn serve  # serves the built dist/ at http://localhost:8080
```

### Testing

Run tests for all workspaces (TypeScript/Jest everywhere, Java via `mvn verify` in `core/`):
```bash
yarn test
```

Java tests:
```bash
cd core && mvn test   # or: cd core && mvn verify
```

JavaScript component tests (uses Jest with ts-jest):
```bash
cd components/melviz-component-echarts
yarn test  # Runs jest --silent --verbose --passWithNoTests
```

Run specific test:
```bash
cd components/melviz-component-echarts
yarn test -- <test-file-pattern>
```

## Architecture Overview

### Monorepo Structure

Melviz is organized as a monorepo with Yarn workspaces:

- **`core/`** - Java/Maven-based backend using GWT (Google Web Toolkit) to compile Java to JavaScript (also a Yarn workspace, `@melviz/core`)
- **`packages/`** - Shared TypeScript libraries and build tooling
- **`components/`** - Independent React-based microfrontend visualization components
- **`webapp/`** - Webpack orchestrator that assembles the final application
- **`examples/`** - Examples gallery: an interactive web app that showcases the dashboard examples

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

1. **Java Core** (`core/melviz-webapp-parent/melviz-webapp-shared/`, `core/melviz-base/melviz-dataset/`, etc.):
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
- `melviz-bom/` - Bill of materials; import in `dependencyManagement` when depending on multiple Melviz artifacts
- `melviz-base/` - Foundational modules: `melviz-json` (JSON handling) and `melviz-dataset` (dataset processing)
- `melviz-shared/` - Shared API contracts: `uberfire-api`, `uberfire-layout-editor-api`, `melviz-displayer-api`, `melviz-navigation-api`, `melviz-services-api`
- `melviz-client/` - GWT-compilable client code: `melviz-common-client`, `melviz-dataset-client`, `melviz-displayer-client`, `melviz-displayer-editor`, `melviz-navigation-client`, `melviz-renderers`, `melviz-patternfly`, `uberfire-layout-editor-client`, `uberfire-runtime-plugins-client`
- `melviz-webapp-parent/` - `melviz-webapp-shared` (shared GWT application) and `melviz-webapp` (main web application assembly, produces WAR)

**Shared Packages** (`packages/`):
- `melviz-component-api` - Component controller and communication interfaces
- `melviz-component-dev` - Development utilities for component testing
- `webpack-base` - Common webpack configuration with TypeScript loader
- `tsconfig` - Shared TypeScript configuration

**Available Components** (`components/`):
- `melviz-component-echarts` - Apache ECharts visualizations
- `melviz-component-echarts-base` - Reusable ECharts wrapper (lives in `components/` but is built as a shared package by `build:packages`)
- `melviz-component-llm-prompter` - LLM prompt engineering UI
- `melviz-component-svg-heatmap` - SVG-based heatmaps

Note: the previous `melviz-component-map` was removed from the repository; a new map component is planned.

### Adding a New Component

1. Create new directory in `components/melviz-component-<name>/`
2. Add `package.json` with dependency on `@melviz/component-api`
3. Create `src/index.tsx` with ComponentController integration
4. Add webpack configuration (can extend `webpack-base`) plus a `dev-webapp/` directory for standalone development
5. Register component in `webapp/package.json` devDependencies
6. Update `webapp/webpack.config.js` to copy component bundle
7. Build with `yarn build` - output goes to `dist/index.js`

### Deployment

The final artifacts are static directories that can be deployed to any static web server or GitHub Pages:
- `webapp/dist/` - the main web application: GWT-compiled Java core (from `core/melviz-webapp-parent/melviz-webapp/target/melviz-webapp/`), all component bundles (from `components/*/dist/`), and static assets
- `examples/dist/` - the examples gallery (built by `yarn build:examples`)

## Working with YAML Dashboards

Melviz renders dashboards defined in YAML. The application can receive content dynamically via `postMessage`:

```javascript
window.postMessage(`pages:
  - components:
    - markdown: "# Hello World!"
`, null)
```

Alternatively, use `setup.js` to configure static dashboards that load on startup.

The canonical set of example dashboards lives in `examples/dashboards/`, organized by category (e.g. `Basic Usage`, `Prometheus`, `Micrometer`, `OpenTelemetry`, `Backstage`, `ansible`, `jupyterhub`, `kepler`, `modelmesh`, `triton`, `misc`). New dashboards are just `.dash.yaml`/`.yml` files dropped into a category folder — the examples gallery discovers them automatically.

## Key Technologies

- **Java**: JDK 17 (pom.xml compiler release and CI use 17; note: core/README.md mentions Java 21 as requirement)
- **Maven**: Build orchestration for Java modules
- **GWT (Google Web Toolkit)**: 2.12.2, compiles Java to JavaScript for client-side execution
- **Yarn**: v4.10.3 for workspace management (`nodeLinker: node-modules`)
- **TypeScript**: 4.6.2 for type-safe component development
- **React**: 17.0.2 for component UI
- **Webpack**: 5.x for module bundling
- **Jest**: 29.x, testing framework with ts-jest for TypeScript
- **Apache ECharts**: 5.x, visualization library used by the echarts component
- **PatternFly**: 5.x, UI components used by the GWT client
- **JSONata**: Data transformation language for dataset processing
- **CI**: JDK 17 and Node.js 18 (see `.github/workflows/`)

## File References

When navigating the codebase:
- Component implementations: [components/](components/)
- Shared APIs: [packages/melviz-component-api/](packages/melviz-component-api/)
- Java core: [core/](core/)
- Final webapp assembly: [webapp/](webapp/)
- Examples gallery and sample dashboards: [examples/](examples/)
- Build configs: webpack.config.js in each package (components also have `dev-webapp/webpack.config.js`)
