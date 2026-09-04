# Installation

## Requirements

=== "Gradle Plugin"

    | Requirement | Version |
    |-------------|---------|
    | Gradle | 7.6.4 – 9.6.1 |
    | JVM | 17+ |

=== "CLI"

    | Requirement | Version |
    |-------------|---------|
    | JVM | 17+ |

### Compatibility Matrix

| Delta Coverage | Gradle | Min JVM |
|----------------|--------|---------|
| **3.x** | 7.6.4 – 9.6.1 | 17 |
| **2.5.x** | 6.7.1 - 8.10.2 | 11 |
| **2.0 - 2.4** | 5.6 - 8.9.x | 11 |
| **1.3.x** | 5.1 - 8.4.x | 11 |

## Install

=== "Kotlin DSL"

    Add Delta Coverage to your **root project's** build file:

    ```kotlin
    plugins {
        id("io.github.gw-kit.delta-coverage") version "3.6.1"
    }
    ```

=== "Groovy DSL"

    Add Delta Coverage to your **root project's** build file:

    ```groovy
    plugins {
        id 'io.github.gw-kit.delta-coverage' version '3.6.1'
    }
    ```

=== "CLI"

    Download the CLI JAR from [Maven Central](https://search.maven.org/artifact/io.github.gw-kit/delta-coverage-cli) or [GitHub Releases](https://github.com/gw-kit/delta-coverage-plugin/releases):

    ```bash
    curl -L -o delta-coverage-cli.jar \
      https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-cli/3.6.1/delta-coverage-cli-3.6.1.jar
    ```

!!! warning "Root project only"
    The Gradle plugin must be applied to the **root project**, not subprojects. It automatically discovers all subprojects with test tasks.

## Verify Installation

=== "Kotlin DSL"

    Run the following command to verify the plugin is installed:

    ```bash
    ./gradlew tasks --group="verification"
    ```

    You should see `deltaCoverage` in the task list:

    ```
    Verification tasks
    ------------------
    deltaCoverage - Generates delta coverage reports for all views
    ```

=== "Groovy DSL"

    Run the following command to verify the plugin is installed:

    ```bash
    ./gradlew tasks --group="verification"
    ```

    You should see `deltaCoverage` in the task list:

    ```
    Verification tasks
    ------------------
    deltaCoverage - Generates delta coverage reports for all views
    ```

=== "CLI"

    Run the following command to verify the CLI is working:

    ```bash
    java -jar delta-coverage-cli.jar --help
    ```

    You should see the usage information printed to the console.

## What Gets Installed

When you apply the Gradle plugin:

1. **JaCoCo plugin** is auto-applied to all projects (configurable)
2. **Delta coverage tasks** are registered for each test task
3. **Report views** are auto-created for `test`, `integrationTest`, etc.

!!! info "CLI Tool"
    The CLI is a standalone JAR with no installation side effects. It reads coverage binary files, class files, and a diff file to produce reports. See the [CLI Reference](../reference/cli.md) for full details.

## Next Steps

- [Quick Start](quick-start.md) - Run your first delta coverage report
- [Configure diff source](../configuration/diff-sources.md) - Set up git comparison
