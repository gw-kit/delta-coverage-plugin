# Delta Coverage

**Code coverage for what matters — your changes**

Delta Coverage is a tool that computes code coverage of new/modified code based on a git diff. Focus on testing what you've changed, not the entire codebase. Available as a Gradle plugin or a standalone CLI tool.

[Get Started](getting-started/quick-start.md){ .md-button .md-button--primary }
[View on GitHub](https://github.com/gw-kit/delta-coverage-plugin){ .md-button }

---

## Why Delta Coverage?

**⚡ Instant Feedback**
:   Get coverage reports for your changes in seconds. No waiting for SonarQube scans or external service delays.

**🧪 Testing Pyramid Support**
:   Separate views for unit tests, integration tests, and E2E tests. Enforce different coverage thresholds per test type.

**🆓 Free, No Infrastructure**
:   Runs locally and in CI. No servers to maintain, no licenses to pay, no external dependencies.

---

## Quick Start

=== "Kotlin DSL"

    **1. Apply the plugin**

    ```kotlin
    plugins {
        id("io.github.gw-kit.delta-coverage") version "3.6.0"
    }
    ```

    **2. Configure diff source**

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        diffSource.git.compareWith.set("refs/remotes/origin/main")
    }
    ```

    **3. Run coverage**

    ```shell
    ./gradlew test deltaCoverage
    ```

=== "Groovy DSL"

    **1. Apply the plugin**

    ```groovy
    plugins {
        id 'io.github.gw-kit.delta-coverage' version '3.6.0'
    }
    ```

    **2. Configure diff source**

    ```groovy
    deltaCoverageReport {
        diffSource.git.compareWith = 'refs/remotes/origin/main'
    }
    ```

    **3. Run coverage**

    ```shell
    ./gradlew test deltaCoverage
    ```

=== "CLI"

    **1. Download the CLI**

    ```bash
    curl -L -o delta-coverage-cli.jar \
      https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-cli/3.6.0/delta-coverage-cli-3.6.0.jar
    ```

    **2. Generate a diff**

    ```bash
    git diff origin/main...HEAD > changes.diff
    ```

    **3. Run coverage**

    ```bash
    java -jar delta-coverage-cli.jar \
      --engine JACOCO \
      --diff-file changes.diff \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java \
      --html --console
    ```

That's it! The plugin auto-discovers your test tasks, generates HTML reports, and enforces coverage on new code.

[Full Quick Start Guide](getting-started/quick-start.md){ .md-button }

---

## Features

- **Two Coverage Engines** — Choose between JaCoCo (standard JVM) or IntelliJ coverage (better for Kotlin)
- **Multiple Report Formats** — HTML, XML, Markdown, and Console reports
- **Automatic Test Discovery** — Report views are auto-created for all test tasks
- **Flexible Diff Sources** — Use file, URL, or git to provide the diff
- **GitHub Actions Integration** — Post coverage reports directly to PR comments
- **[CLI Tool](reference/cli.md)** — Run delta coverage without Gradle plugin

---

## How It Works

Delta Coverage takes your test coverage data and filters it to show only the lines you've changed. This gives you actionable feedback on your work, not noise from legacy code.

![Delta Coverage HTML Report](https://user-images.githubusercontent.com/8483470/77781538-a74f3480-704d-11ea-9e39-051f1001b88a.png)

---

## What Users Say

> "Delta Coverage made our code reviews faster. We stopped arguing about which code needs tests — the report shows it clearly."

> "Onboarding new developers is easier. They can focus on testing their changes without worrying about overall project coverage."

---

## Get Started

Ready to try Delta Coverage? The [Quick Start guide](getting-started/quick-start.md) will have you running in under 5 minutes.

For a comparison with other tools, see [Delta Coverage vs SonarQube](comparison/vs-sonarqube.md).
