# Quick Start

Get Delta Coverage running in 3 steps.

## Prerequisites

=== "Kotlin DSL"

    - Gradle 7.6.4 or newer
    - JVM 17 or newer
    - A git repository with tests

=== "Groovy DSL"

    - Gradle 7.6.4 or newer
    - JVM 17 or newer
    - A git repository with tests

=== "CLI"

    - JVM 17 or newer
    - Coverage binary files (e.g., `.exec` from JaCoCo)
    - Compiled class files
    - A diff file (unified diff format)

## Step 1: Install

=== "Kotlin DSL"

    Add the Delta Coverage plugin to your root project's `build.gradle.kts`:

    ```kotlin
    plugins {
        id("io.github.gw-kit.delta-coverage") version "3.6.0"
    }
    ```

=== "Groovy DSL"

    Add the Delta Coverage plugin to your root project's `build.gradle`:

    ```groovy
    plugins {
        id 'io.github.gw-kit.delta-coverage' version '3.6.0'
    }
    ```

=== "CLI"

    Download the CLI JAR:

    ```bash
    curl -L -o delta-coverage-cli.jar \
      https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-cli/3.6.0/delta-coverage-cli-3.6.0.jar
    ```

!!! note "Root project only"
    The Gradle plugin must be applied to the root project. It automatically discovers all subprojects with tests.

## Step 2: Configure Diff Source

Tell Delta Coverage what to compare against. The most common setup compares with your main branch:

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        diffSource.git.compareWith.set("refs/remotes/origin/main")
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        diffSource.git.compareWith = 'refs/remotes/origin/main'
    }
    ```

=== "CLI"

    Generate a diff file from git:

    ```bash
    git diff origin/main...HEAD > changes.diff
    ```

## Step 3: Run Coverage

=== "Kotlin DSL"

    Run your tests and generate the delta coverage report:

    ```shell
    ./gradlew test deltaCoverage
    ```

=== "Groovy DSL"

    Run your tests and generate the delta coverage report:

    ```shell
    ./gradlew test deltaCoverage
    ```

=== "CLI"

    Run the CLI with your coverage data:

    ```bash
    java -jar delta-coverage-cli.jar \
      --engine JACOCO \
      --diff-file changes.diff \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java \
      --html --console
    ```

## Expected Output

The console will show a coverage summary:

```
+----------------------+----------+----------+--------+
| [test] Delta Coverage Stats                         |
+----------------------+----------+----------+--------+
| Class                | Lines    | Branches | Instr. |
+----------------------+----------+----------+--------|
| com.example.MyClass  | 85.71%   | 75%      | 88.24% |
+----------------------+----------+----------+--------+
| Total                | 85.71%   | 75%      | 88.24% |
+----------------------+----------+----------+--------+
```

HTML reports are generated in `build/reports/coverage-reports/delta-coverage/`.

## What Just Happened?

=== "Kotlin DSL"

    1. **Test Discovery** — The plugin found your test tasks automatically
    2. **Coverage Collection** — JaCoCo collected coverage data during tests
    3. **Diff Analysis** — Git diff was computed against `origin/main`
    4. **Filtered Report** — Coverage was filtered to show only changed lines

=== "Groovy DSL"

    1. **Test Discovery** — The plugin found your test tasks automatically
    2. **Coverage Collection** — JaCoCo collected coverage data during tests
    3. **Diff Analysis** — Git diff was computed against `origin/main`
    4. **Filtered Report** — Coverage was filtered to show only changed lines

=== "CLI"

    1. **Diff Parsing** — The CLI read your diff file to identify changed lines
    2. **Coverage Loading** — Coverage binary files were loaded and analyzed
    3. **Class Matching** — Changed files were matched to compiled class files
    4. **Filtered Report** — Coverage was filtered to show only changed lines

## Next Steps

- [Configure violation rules](../configuration/violation-rules.md) to enforce coverage thresholds
- [Set up CI integration](../guides/ci-integration.md) to run on every PR
- [Add PR comments](../guides/pr-comments.md) with coverage reports
