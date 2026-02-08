# Contributing to Documentation

This guide explains how to work with the Delta Coverage documentation site locally.

## Prerequisites

- Python 3.8 or newer
- pip (Python package manager)

## Setup

Install MkDocs Material:

```bash
pip install mkdocs-material
```

## Local Development

Start the local development server:

```bash
mkdocs serve
```

Open http://127.0.0.1:8000 in your browser.

The server auto-reloads when you save changes to any `.md` file.

## Verify Before Committing

Build the site to check for errors:

```bash
mkdocs build --strict
```

This will fail if:
- Any linked page is missing
- Navigation references non-existent files
- Markdown syntax errors exist

## File Structure

```
docs/
├── index.md                 # Landing page
├── getting-started/         # Installation and quick start
├── configuration/           # Plugin configuration options
├── guides/                  # How-to guides
├── comparison/              # Comparisons with other tools
├── recipes/                 # Copy-paste configurations
├── faq.md                   # Frequently asked questions
└── changelog.md             # Release notes

mkdocs.yml                   # Site configuration
```

## Adding a New Page

1. Create the `.md` file in the appropriate directory
2. Add it to the `nav` section in `mkdocs.yml`
3. Run `mkdocs build --strict` to verify

## Common Markdown Extensions

The site supports these extensions:

### Admonitions (callouts)

```markdown
!!! note "Optional title"
    This is a note.

!!! warning
    This is a warning.

!!! info "Coming soon"
    Page under construction.
```

### Tabbed content

```markdown
=== "Kotlin DSL"

    ```kotlin
    plugins {
        id("io.github.gw-kit.delta-coverage")
    }
    ```

=== "Groovy DSL"

    ```groovy
    plugins {
        id 'io.github.gw-kit.delta-coverage'
    }
    ```
```

### Code blocks with copy button

````markdown
```kotlin
// Code here - copy button appears automatically
```
````

## Deployment

Documentation is automatically deployed to GitHub Pages when changes are pushed to `main` branch. The workflow is defined in `.github/workflows/deploy-docs.yml`.

Manual deployment (if needed):

```bash
mkdocs gh-deploy --force
```
