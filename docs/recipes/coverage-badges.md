# Coverage Badges

Display coverage badges in your README using Delta Coverage's full coverage reports.

## Overview

Delta Coverage can generate full coverage reports alongside delta reports. Use these to create badges showing overall project coverage.

## Configuration

Enable full coverage reports:

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith("refs/remotes/origin/main")

    reports {
        xml.set(true)
        fullCoverageReport.set(true)
    }
}
```

## GitHub Actions Workflow

Generate badges and commit them to a separate branch:

```yaml
name: Coverage Badge

on:
  push:
    branches: [main]

jobs:
  badge:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run tests
        run: ./gradlew test deltaCoverage

      - name: Generate badge
        uses: cicirello/jacoco-badge-generator@v2
        with:
          jacoco-csv-file: build/reports/coverage-reports/full-coverage-report/test/report.csv
          badges-directory: .badges

      - name: Commit badge
        uses: EndBug/add-and-commit@v9
        with:
          add: '.badges'
          message: 'Update coverage badge'
          default_author: github_actions
```

## Using shields.io

Create dynamic badges using shields.io with your coverage data.

### Store Coverage Percentage

Add a step to extract and store coverage:

```yaml
- name: Extract coverage
  id: coverage
  run: |
    # Extract from XML or console output
    COVERAGE=$(grep -oP 'Total.*?\K\d+(?:\.\d+)?(?=%)' build/reports/coverage.txt | head -1)
    echo "percentage=$COVERAGE" >> $GITHUB_OUTPUT

- name: Create badge JSON
  run: |
    echo '{"schemaVersion":1,"label":"coverage","message":"${{ steps.coverage.outputs.percentage }}%","color":"green"}' > coverage.json
```

### Badge URL

```markdown
![Coverage](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/owner/repo/main/coverage.json)
```

## Using gist-based Badges

Store badge data in a GitHub Gist:

```yaml
- name: Update gist
  uses: schneegans/dynamic-badges-action@v1.7.0
  with:
    auth: ${{ secrets.GIST_TOKEN }}
    gistID: your-gist-id
    filename: coverage.json
    label: Coverage
    message: ${{ steps.coverage.outputs.percentage }}%
    color: green
```

Badge URL:
```markdown
![Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/user/gist-id/raw/coverage.json)
```

## README Example

```markdown
# My Project

![Build](https://github.com/owner/repo/actions/workflows/build.yml/badge.svg)
![Coverage](https://img.shields.io/endpoint?url=...)

## Description
...
```

## Per-View Badges

Create separate badges for different test types:

```yaml
- name: Unit test badge
  uses: cicirello/jacoco-badge-generator@v2
  with:
    jacoco-csv-file: build/reports/coverage-reports/full-coverage-report/test/report.csv
    badges-directory: .badges
    badge-name: unit-coverage

- name: Integration test badge
  uses: cicirello/jacoco-badge-generator@v2
  with:
    jacoco-csv-file: build/reports/coverage-reports/full-coverage-report/integrationTest/report.csv
    badges-directory: .badges
    badge-name: integration-coverage
```

Display in README:

```markdown
![Unit Tests](/.badges/unit-coverage.svg)
![Integration Tests](/.badges/integration-coverage.svg)
```

## Delta Coverage Action

The [delta-coverage-action](https://github.com/gw-kit/delta-coverage-action) can also generate badges. See the action documentation for details.
