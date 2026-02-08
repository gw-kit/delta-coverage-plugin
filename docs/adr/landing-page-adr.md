# ADR: Documentation Site Implementation

**Status:** Proposed
**Date:** 2025-02-07
**Author:** Sergii
**Project:** delta-coverage-gradle-plugin

## Context and Problem Statement

The current README.md is ~500 lines — comprehensive but overwhelming for newcomers evaluating the plugin. Key issues:

1. Poor discoverability — single page, not indexed well by Google
2. No landing page experience — users must scroll through everything
3. Hard to navigate — configuration, guides, and examples mixed together
4. No separation between quick-start and deep-dive content

A dedicated documentation site would improve discoverability, navigation, and first impressions.

## Decision Drivers

- **Discoverability** — each page indexed by Google separately
- **Navigation** — users land directly on what they need
- **Maintainability** — docs versioned with code in `docs/` folder
- **Professional appearance** — builds trust with potential adopters
- **Cost** — free hosting

## Considered Options

### Option 1: Enhanced README Only

Keep everything in README.md, improve organization with better headings.

**Pros:**
- Single source of truth
- No additional tooling

**Cons:**
- Still overwhelming for newcomers
- Poor Google indexing (single page)
- No landing page experience

### Option 2: GitHub Wiki

Use GitHub's built-in wiki.

**Pros:**
- No deployment needed
- Integrated with repo

**Cons:**
- Not indexed by search engines
- Limited customization
- Cannot be versioned with code

### Option 3: MkDocs Material (Selected)

Static documentation site with MkDocs Material theme, hosted on GitHub Pages.

**Pros:**
- Professional appearance out of the box
- Each page indexed by Google
- Dark mode, search, code highlighting, tabs built-in
- Versioned with code (`docs/` folder)
- Free hosting on GitHub Pages
- Standard in JVM ecosystem (Gradle, Detekt, Kotest)

**Cons:**
- Requires Python for local preview
- Additional CI workflow for deployment

## Decision Outcome

**Chosen option: MkDocs Material with GitHub Pages hosting**

---

## Site Structure

```
docs/
├── index.md                    # Landing page
├── getting-started/
│   ├── installation.md         # Plugin apply + minimal config
│   ├── quick-start.md          # 3-step flow with expected output
│   └── migration.md            # surpsg → gw-kit plugin ID migration
├── configuration/
│   ├── diff-sources.md         # git / file / url
│   ├── coverage-engines.md     # JaCoCo vs IntelliJ
│   ├── report-views.md         # Views feature
│   ├── violation-rules.md      # Rule options with examples
│   ├── reports.md              # HTML/XML/Markdown/Console
│   └── exclude-classes.md      # Exclusion patterns
├── guides/
│   ├── local-development.md    # Local workflow
│   ├── ci-integration.md       # GitHub Actions, GitLab CI, etc.
│   ├── pr-comments.md          # delta-coverage-action setup
│   ├── kotlin-projects.md      # IntelliJ engine + CoverJet
│   ├── multi-module.md         # Aggregated views
│   └── testing-pyramid.md      # Views for unit/integration/e2e
├── comparison/
│   └── vs-sonarqube.md         # Comparison with alternatives
├── recipes/
│   ├── enforce-90-percent.md   # Copy-paste config
│   ├── legacy-projects.md      # Incremental improvement
│   └── coverage-badges.md      # Badge generation
├── changelog.md
└── faq.md
```

---

## Landing Page Structure (index.md)

### Hero Section
- Headline + subheadline
- GIF showing workflow (edit → test → deltaCoverage → result)
- Primary CTA: "Get Started"

### Benefits Section (3 cards)
1. Instant local feedback
2. Testing pyramid support
3. Free, no infrastructure

### Quick Start Snippet
- Minimal build.gradle.kts config
- Single command to run

### Social Proof
- Usage stats / testimonials

---

## MkDocs Configuration

```yaml
# mkdocs.yml
site_name: Delta Coverage
site_url: https://gw-kit.github.io/delta-coverage-plugin
repo_url: https://github.com/gw-kit/delta-coverage-plugin
repo_name: gw-kit/delta-coverage-plugin

theme:
  name: material
  palette:
    - scheme: default
      primary: green
      toggle:
        icon: material/brightness-7
        name: Switch to dark mode
    - scheme: slate
      primary: green
      toggle:
        icon: material/brightness-4
        name: Switch to light mode
  features:
    - navigation.sections
    - navigation.expand
    - navigation.tabs
    - content.code.copy
    - search.suggest
    - search.highlight

markdown_extensions:
  - admonition
  - pymdownx.details
  - pymdownx.superfences
  - pymdownx.tabbed:
      alternate_style: true
  - pymdownx.highlight:
      anchor_linenums: true
  - attr_list
  - def_list

nav:
  - Home: index.md
  - Getting Started:
    - Installation: getting-started/installation.md
    - Quick Start: getting-started/quick-start.md
    - Migration: getting-started/migration.md
  - Configuration:
    - Diff Sources: configuration/diff-sources.md
    - Coverage Engines: configuration/coverage-engines.md
    - Report Views: configuration/report-views.md
    - Violation Rules: configuration/violation-rules.md
    - Reports: configuration/reports.md
    - Exclude Classes: configuration/exclude-classes.md
  - Guides:
    - Local Development: guides/local-development.md
    - CI Integration: guides/ci-integration.md
    - PR Comments: guides/pr-comments.md
    - Kotlin Projects: guides/kotlin-projects.md
    - Multi-Module: guides/multi-module.md
    - Testing Pyramid: guides/testing-pyramid.md
  - Comparison: comparison/vs-sonarqube.md
  - Recipes:
    - Enforce 90% Coverage: recipes/enforce-90-percent.md
    - Legacy Projects: recipes/legacy-projects.md
    - Coverage Badges: recipes/coverage-badges.md
  - FAQ: faq.md
  - Changelog: changelog.md
```

---

## GitHub Actions Deployment

```yaml
# .github/workflows/deploy-docs.yml
name: Deploy docs

on:
  push:
    branches: [main]
    paths: ['docs/**', 'mkdocs.yml']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.12'
      - run: pip install mkdocs-material
      - run: mkdocs gh-deploy --force
```

---

## Implementation Phases

### Phase 1: Foundation

**Scope:**
- [ ] Create `mkdocs.yml`
- [ ] Create `docs/index.md` (landing page)
- [ ] Create `docs/getting-started/quick-start.md`
- [ ] Create `docs/comparison/vs-sonarqube.md`
- [ ] Set up GitHub Actions deployment

**Deliverable:** Live site at `https://gw-kit.github.io/delta-coverage-plugin`

### Phase 2: Core Documentation

**Scope:**
- [ ] All getting-started pages
- [ ] All configuration pages
- [ ] `guides/local-development.md`
- [ ] `guides/ci-integration.md`

### Phase 3: Complete Documentation

**Scope:**
- [ ] Remaining guides
- [ ] All recipes
- [ ] FAQ
- [ ] Migrate README content
- [ ] Slim down README to point to docs

### Phase 4: Polish

**Scope:**
- [ ] Add GIF to landing page
- [ ] Add screenshots
- [ ] Review all pages

---

## Local Development

```bash
# Install
pip install mkdocs-material

# Serve locally
mkdocs serve

# Build
mkdocs build
```

---

## Open Questions

1. **Custom domain?** — `delta-coverage.dev` or use GitHub Pages default?
2. **Versioned docs?** — Show docs for different plugin versions?
3. **Search analytics?** — Track what users search for?

---

## Related Documents

- [Marketing Strategy](marketing-strategy.md) — positioning, messaging, branding
- [Content Plan](content-plan.md) — GIF, demo repo, blog post, PR screenshots

---

## References

- [MkDocs Material](https://squidfunk.github.io/mkdocs-material/)
- [GitHub Pages](https://pages.github.com/)
- [Detekt docs](https://detekt.dev/) — similar tool example
- [Kotest docs](https://kotest.io/) — similar tool example

---

## Changelog

| Date       | Version | Changes                          |
|------------|---------|----------------------------------|
| 2025-02-07 | 0.2     | Split into focused ADR, extracted marketing and content to separate files |
| 2025-02-07 | 0.1     | Initial draft                    |