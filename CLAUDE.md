# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

The compiled web application will be in `core/melviz-webapp-parent/melviz-webapp/target/melviz-webapp/`.

### JavaScript Components and Webapp

Install dependencies once from the repository root:
```bash
yarn install
```

Build all components:
```bash
# From repository root - builds all workspace packages in topological order
# The -t flag ensures dependencies are built before dependents
yarn workspaces foreach -At run build
```

Build specific component:
```bash
cd components/melviz-component-echarts
yarn build  # Runs tests, cleans dist, then webpack
```

Build final webapp (assembles all components):
```bash
cd webapp
yarn build  # Cleans dist and runs webpack to merge everything
```

### Development Mode

Run a component in dev mode with hot reload:
```bash
cd components/melviz-component-echarts
yarn start  # Starts webpack-dev-server on port 9001
```

### Testing

Java tests:
```bash
cd core && mvn test
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

- **Java**: JDK 17 (note: core/README.md mentions Java 21 as requirement, but pom.xml uses 17)
- **Maven**: Build orchestration for Java modules
- **GWT (Google Web Toolkit)**: Compiles Java to JavaScript for client-side execution
- **Yarn**: v4.10.3 for workspace management
- **TypeScript**: 4.6.2 for type-safe component development
- **React**: 17.0.2 for component UI
- **Webpack**: 5.x for module bundling
- **Jest**: Testing framework with ts-jest for TypeScript
- **Apache ECharts**: Visualization library used by echarts component
- **JSONata**: Data transformation language for dataset processing

## File References

When navigating the codebase:
- Component implementations: [components/](components/)
- Shared APIs: [packages/melviz-component-api/](packages/melviz-component-api/)
- Java core: [core/](core/)
- Final webapp assembly: [webapp/](webapp/)
- Build configs: [webpack.config.js files in each package]
