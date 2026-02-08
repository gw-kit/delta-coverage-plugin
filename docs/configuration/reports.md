# Reports

Delta Coverage generates reports in multiple formats. All reports show only coverage for changed code.

## Report Formats

| Format | Default | Description |
|--------|---------|-------------|
| Console | `true` | ASCII table printed to terminal |
| HTML | `false` | Interactive browser report |
| XML | `false` | Machine-readable for CI tools |
| Markdown | `false` | For PR comments |

## Configuration

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        reports {
            console.set(true)   // default
            html.set(true)
            xml.set(true)
            markdown.set(true)
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        reports {
            console = true   // default
            html = true
            xml = true
            markdown = true
        }
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --console \
      --html \
      --xml \
      --markdown \
      --diff-file changes.diff \
      --engine JACOCO \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java
    ```

## Output Location

=== "Gradle Plugin"

    Reports are generated in `build/reports/coverage-reports/delta-coverage/<view>/`:

    ```
    build/reports/coverage-reports/
    └── delta-coverage/
        └── test/
            ├── html/
            │   └── index.html
            ├── report.xml
            └── report.md
    ```

=== "CLI"

    Reports are generated in `build/reports/delta-coverage/`:

    ```
    build/reports/delta-coverage/
    ├── html/
    │   └── index.html
    ├── report.xml
    └── report.md
    ```

    !!! info
        The CLI does not use report views, so there is no `<view>` subdirectory.

### Custom Output Directory

=== "Kotlin DSL"

    ```kotlin
    reports {
        reportDir.set("custom/reports/path")
    }
    ```

=== "Groovy DSL"

    ```groovy
    reports {
        reportDir = 'custom/reports/path'
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --report-dir custom/reports/path \
      ...
    ```

## Console Report

Printed automatically after each run:

```
+----------------------+----------+----------+--------+
| [test] Delta Coverage Stats                         |
+----------------------+----------+----------+--------+
| Class                | Lines    | Branches | Instr. |
+----------------------+----------+----------+--------|
| com.example.MyClass  | 85.71%   | 75%      | 88.24% |
| com.example.Service  | 92.31%   | 100%     | 94.12% |
+----------------------+----------+----------+--------+
| Total                | ✔ 88.89% | ✔ 83.33% | ✔ 91%  |
+----------------------+----------+----------+--------+
| Min expected         | 80%      | 70%      | 80%    |
+----------------------+----------+----------+--------+
```

## HTML Report

Standard JaCoCo HTML report filtered to show only changed code:

=== "Kotlin DSL"

    ```kotlin
    reports {
        html.set(true)
    }
    ```

=== "Groovy DSL"

    ```groovy
    reports {
        html = true
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar --html ...
    ```

=== "Gradle Plugin"

    Open `build/reports/coverage-reports/delta-coverage/test/html/index.html` in a browser.

=== "CLI"

    Open `build/reports/delta-coverage/html/index.html` in a browser.

![Delta Coverage HTML Report](https://user-images.githubusercontent.com/8483470/77781538-a74f3480-704d-11ea-9e39-051f1001b88a.png)

## Markdown Report

Ideal for posting to PR comments:

=== "Kotlin DSL"

    ```kotlin
    reports {
        markdown.set(true)
    }
    ```

=== "Groovy DSL"

    ```groovy
    reports {
        markdown = true
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar --markdown ...
    ```

Output (`report.md`):

| Class | Lines | Branches | Instr. |
|-------|-------|----------|--------|
| MyClass | 85.71% | 75% | 88.24% |
| Service | 92.31% | 100% | 94.12% |
| Total | 🟢 88.89% | 🟢 83.33% | 🟢 91% |
| Min expected | 80% | 70% | 80% |

## XML Report

For integration with CI tools and coverage aggregators:

=== "Kotlin DSL"

    ```kotlin
    reports {
        xml.set(true)
    }
    ```

=== "Groovy DSL"

    ```groovy
    reports {
        xml = true
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar --xml ...
    ```

## Full Coverage Reports

Generate full coverage reports alongside delta reports:

=== "Kotlin DSL"

    ```kotlin
    reports {
        html.set(true)
        fullCoverageReport.set(true)
    }
    ```

=== "Groovy DSL"

    ```groovy
    reports {
        html = true
        fullCoverageReport = true
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar --html --full-coverage ...
    ```

This creates two report sets:

```
build/reports/coverage-reports/
├── delta-coverage/       # Only changed code
│   └── test/
└── full-coverage-report/ # Entire codebase
    └── test/
```

Use cases:

- Compare delta vs full coverage
- Generate coverage badges from full report
- Track overall project health

## Recommended Setup

### Local Development

=== "Kotlin DSL"

    ```kotlin
    reports {
        console.set(true)
        html.set(true)
    }
    ```

=== "Groovy DSL"

    ```groovy
    reports {
        console = true
        html = true
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar --console --html ...
    ```

### CI Pipeline

=== "Kotlin DSL"

    ```kotlin
    reports {
        console.set(true)
        html.set(true)
        markdown.set(true)  // For PR comments
        xml.set(true)       // For coverage tools
    }
    ```

=== "Groovy DSL"

    ```groovy
    reports {
        console = true
        html = true
        markdown = true  // For PR comments
        xml = true       // For coverage tools
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --console --html --markdown --xml ...
    ```
