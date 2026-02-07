# Migration Guide

## Migrating from v2.x to v3.x

### Plugin ID Change

The plugin ID changed from `io.github.surpsg.delta-coverage` to `io.github.gw-kit.delta-coverage`:

=== "Before (v2.x)"

    ```kotlin
    plugins {
        id("io.github.surpsg.delta-coverage") version "2.5.0"
    }
    ```

=== "After (v3.x)"

    ```kotlin
    plugins {
        id("io.github.gw-kit.delta-coverage") version "3.6.0"
    }
    ```

### JVM Requirement

v3.x requires JVM 17 or newer. If you're on JVM 11, stay on v2.5.x.

### Configuration Changes

The configuration DSL remains compatible. No changes required to your `deltaCoverageReport` block.

### Report Views

Report views are now auto-discovered. If you manually registered views, you can remove the registration code:

=== "Before (v2.x)"

    ```kotlin
    configure<DeltaCoverageConfiguration> {
        reportViews {
            register("test") {
                // configuration
            }
        }
    }
    ```

=== "After (v3.x)"

    ```kotlin
    configure<DeltaCoverageConfiguration> {
        reportViews {
            val test by getting {
                // configuration
            }
        }
    }
    ```

## Migrating from v1.x to v2.x

### Breaking Changes

1. **Minimum Gradle version** increased to 5.6
2. **Configuration DSL** changed significantly

See the [v2 migration guide](https://github.com/gw-kit/delta-coverage-plugin/blob/2.5.0/README.md) for details.

## Getting Help

If you encounter issues during migration, [open an issue](https://github.com/gw-kit/delta-coverage-plugin/issues) on GitHub.
