# GitHub Actions Workflows

This directory contains automated workflows for the Melviz project. These workflows handle continuous integration, security scanning, dependency management, and release automation.

## Available Workflows

### 1. Java CI ([ci-java.yml](ci-java.yml))
**Triggers:** Push/PR to main branch (when `core/**` changes)

Builds and tests the Java/Maven core modules:
- Sets up JDK 17 with Maven caching
- Runs `mvn clean install` and `mvn test`
- Uploads GWT webapp artifact for 7 days

**Artifact:** `gwt-webapp` - GWT-compiled application from `core/melviz-webapp-parent/melviz-webapp/target/`

---

### 2. JavaScript CI ([ci-javascript.yml](ci-javascript.yml))
**Triggers:** Push/PR to main branch (when `packages/`, `components/`, `webapp/`, or dependency files change)

Builds and tests all JavaScript/TypeScript workspace packages:
- Sets up Node.js 18 with Yarn caching
- Installs dependencies with `yarn install --immutable`
- Builds all workspaces with `yarn workspaces foreach -A run build`
- Runs tests with `yarn workspaces foreach -A run test`
- Uploads component artifacts for 7 days

**Artifact:** `components` - Built component bundles from `components/*/dist/`

---

### 3. Build and Publish Webapp ([build-publish-webapp.yml](build-publish-webapp.yml))
**Triggers:**
- Push to main branch
- Release published
- Version tags (`v*`)
- Manual trigger (`workflow_dispatch`)

Complete end-to-end build of the entire application:
1. Builds Java core with Maven
2. Builds all JavaScript components and webapp
3. Creates `.tar.gz` and `.zip` archives of `webapp/dist/`
4. Uploads artifacts for 30 days
5. Attaches to GitHub releases (if triggered by release)
6. Updates "latest" pre-release tag on main branch pushes

**Artifacts:**
- `melviz-webapp.tar.gz` - Compressed webapp distribution
- `melviz-webapp.zip` - Zipped webapp distribution

**Download:** Artifacts are available in the Actions tab or from GitHub Releases.

---

### 4. Pull Request Validation ([pr-validation.yml](pr-validation.yml))
**Triggers:** Pull requests to main branch

Smart validation that only runs affected builds:
- Uses path filters to detect Java or JavaScript changes
- Runs appropriate build/test steps based on changes
- Posts validation status comment to PR
- Avoids unnecessary builds when only specific files change

---

### 5. Dependency Review ([dependency-review.yml](dependency-review.yml))
**Triggers:** Pull requests to main branch

Security and license compliance:
- Scans dependency changes in PRs
- Fails on moderate+ severity vulnerabilities
- Blocks GPL-2.0 and GPL-3.0 licenses
- Posts summary comment to PR

---

### 6. CodeQL Security Analysis ([codeql.yml](codeql.yml))
**Triggers:**
- Push/PR to main branch
- Weekly schedule (Mondays at midnight)

Static analysis for security vulnerabilities:
- Analyzes Java and JavaScript code separately
- Scans for common vulnerabilities (injection, XSS, etc.)
- Results appear in Security tab
- Runs weekly to catch new vulnerability patterns

---

### 7. Mark Stale Issues and PRs ([stale.yml](stale.yml))
**Triggers:** Daily schedule and manual

Housekeeping automation:
- Marks issues/PRs stale after 60 days of inactivity
- Closes stale items after 14 additional days
- Exempts items labeled: `pinned`, `security`, `enhancement`

---

## Downloading the Webapp

### From GitHub Actions
1. Go to **Actions** tab → **Build and Publish Webapp**
2. Click on a successful workflow run
3. Scroll to **Artifacts** section
4. Download `melviz-webapp.tar.gz` or `melviz-webapp.zip`

### From GitHub Releases
When a release is published:
1. Go to **Releases** section
2. Download `melviz-webapp.tar.gz` or `melviz-webapp.zip` from assets

### Latest Build (Pre-release)
The "latest" tag always contains the most recent main branch build:
1. Go to **Releases** → **latest** tag
2. Download the webapp archives

---

## Required Repository Secrets

Currently, all workflows use `GITHUB_TOKEN` (automatically provided). No additional secrets are required.

---

## Local Testing

To test workflows locally, use [act](https://github.com/nektos/act):

```bash
# Test Java CI
act push -j build-and-test -W .github/workflows/ci-java.yml

# Test JavaScript CI
act push -j build-and-test -W .github/workflows/ci-javascript.yml

# Test full webapp build
act push -j build-full-webapp -W .github/workflows/build-publish-webapp.yml
```

---

## Maintenance

### Adding New Component Tests
Component tests are automatically included when using the standard `test` script in `package.json`. No workflow changes needed.

### Updating Node.js/Java Versions
Update version numbers in:
- [ci-java.yml](ci-java.yml) - `java-version`
- [ci-javascript.yml](ci-javascript.yml) - `node-version`
- [build-publish-webapp.yml](build-publish-webapp.yml) - Both `java-version` and `node-version`
- [pr-validation.yml](pr-validation.yml) - Both versions
- [codeql.yml](codeql.yml) - Both versions

### Adjusting Artifact Retention
- CI artifacts: 7 days (suitable for troubleshooting recent builds)
- Webapp artifacts: 30 days (longer retention for deployments)
- Modify `retention-days` in workflow files as needed
