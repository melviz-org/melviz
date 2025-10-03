# Melviz Core
[![Build Melviz and Upload Artifacts](https://github.com/OpenMelViz/melviz-core/actions/workflows/maven.yml/badge.svg)](https://github.com/OpenMelViz/melviz-core/actions/workflows/maven.yml)

This is the core Melviz project. It results in a web application that can be embed or live standalone to render YAML contents.

## Requirements

* Java 21
* Maven

## Building

Run `mvn clean install` on project root and find the web application in directory `melviz-webapp-parent/melviz-webapp/target/melviz-webapp`. 

The web application can receive dynamic content by posting YAML to the main frame. Here's a sample Javascript code:

```
window.postMessage(`pages:    
  - components:
    - markdown: "# Hello World!"
`, null)
```

It is also possible to use `setup.js` to configure static dashboards.

Melviz can run in any web server or in Github Pages.



