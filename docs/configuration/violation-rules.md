# Violation Rules

Violation rules enforce minimum coverage thresholds on new/modified code.

## Quick Setup

Enforce 90% coverage on all metrics:

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        reportViews {
            val test by getting {
                violationRules.failIfCoverageLessThan(0.9)
            }
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        reportViews {
            test {
                violationRules.failIfCoverageLessThan(0.9)
            }
        }
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --min-coverage 0.9 \
      --fail-on-violation \
      --diff-file changes.diff \
      --engine JACOCO \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java \
      --console
    ```

This sets 90% minimum for lines, branches, and instructions, and fails the build if not met.

## Coverage Entities

Three coverage metrics are available:

| Entity | Description |
|--------|-------------|
| `LINE` | Source code lines |
| `BRANCH` | Decision branches (if/else, switch) |
| `INSTRUCTION` | Bytecode instructions |

## Configuring Rules

### Per-Entity Rules

Set different thresholds per entity:

=== "Kotlin DSL"

    ```kotlin
    violationRules {
        rule(CoverageEntity.LINE) {
            minCoverageRatio.set(0.8)
        }
        rule(CoverageEntity.BRANCH) {
            minCoverageRatio.set(0.7)
        }
        rule(CoverageEntity.INSTRUCTION) {
            minCoverageRatio.set(0.9)
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    violationRules {
        rule(CoverageEntity.LINE) {
            minCoverageRatio = 0.8
        }
        rule(CoverageEntity.BRANCH) {
            minCoverageRatio = 0.7
        }
        rule(CoverageEntity.INSTRUCTION) {
            minCoverageRatio = 0.9
        }
    }
    ```

=== "CLI"

    The CLI supports a single `--min-coverage` threshold applied to all entities:

    ```bash
    java -jar delta-coverage-cli.jar --min-coverage 0.8 --fail-on-violation ...
    ```

    !!! info "Per-entity rules"
        For per-entity thresholds with the CLI, use a [configuration file](../reference/cli.md#configuration-file).

### Kotlin DSL Shorthand

```kotlin
violationRules {
    CoverageEntity.LINE {
        minCoverageRatio.set(0.8)
    }
    CoverageEntity.BRANCH {
        minCoverageRatio.set(0.7)
    }
}
```

### All Entities at Once

=== "Kotlin DSL"

    ```kotlin
    violationRules {
        all {
            minCoverageRatio.set(0.8)
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    violationRules {
        all {
            minCoverageRatio = 0.8
        }
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar --min-coverage 0.8 ...
    ```

## Fail on Violation

Control whether violations fail the build:

=== "Kotlin DSL"

    ```kotlin
    violationRules {
        failOnViolation.set(true)  // default: false

        rule(CoverageEntity.LINE) {
            minCoverageRatio.set(0.8)
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    violationRules {
        failOnViolation = true  // default: false

        rule(CoverageEntity.LINE) {
            minCoverageRatio = 0.8
        }
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --min-coverage 0.8 \
      --fail-on-violation \
      ...
    ```

!!! note
    `failIfCoverageLessThan()` automatically sets `failOnViolation = true`.

## Entity Count Threshold

Ignore violations if too few entities were changed:

```kotlin
violationRules {
    rule(CoverageEntity.BRANCH) {
        minCoverageRatio.set(0.8)
        entityCountThreshold.set(10)  // Ignore if < 10 branches changed
    }
}
```

This is useful for small changes where a single missed branch would cause a large percentage drop.

!!! info "CLI"
    Entity count thresholds are not available as CLI flags. Use a [configuration file](../reference/cli.md#configuration-file) for advanced violation rules.

## Example Output

### Passing

```
> Task :deltaCoverage
Fail on violations: true. Found violations: 0.
```

### Failing

```
> Task :deltaCoverage FAILED

Fail on violations: true. Found violations: 2.

FAILURE: Build failed with an exception.

> java.lang.Exception: Rule violated for bundle test:
  instructions covered ratio is 0.5, but expected minimum is 0.9

  [test] Rule violated for bundle test:
  lines covered ratio is 0.0, but expected minimum is 0.9
```

## Best Practices

1. **Start with 80%** and increase gradually
2. **Use `entityCountThreshold`** for branch coverage to avoid noise on small changes
3. **Different thresholds per view** — lower for integration tests, higher for unit tests
4. **Don't set 100%** — it's often impractical and leads to low-value tests

## Full Example

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        diffSource.git.compareWith.set("refs/remotes/origin/main")

        reportViews {
            val test by getting {
                violationRules {
                    failOnViolation.set(true)

                    rule(CoverageEntity.LINE) {
                        minCoverageRatio.set(0.85)
                    }
                    rule(CoverageEntity.BRANCH) {
                        minCoverageRatio.set(0.75)
                        entityCountThreshold.set(5)
                    }
                    rule(CoverageEntity.INSTRUCTION) {
                        minCoverageRatio.set(0.80)
                    }
                }
            }
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        diffSource.git.compareWith = 'refs/remotes/origin/main'

        reportViews {
            test {
                violationRules {
                    failOnViolation = true

                    rule(CoverageEntity.LINE) {
                        minCoverageRatio = 0.85
                    }
                    rule(CoverageEntity.BRANCH) {
                        minCoverageRatio = 0.75
                        entityCountThreshold = 5
                    }
                    rule(CoverageEntity.INSTRUCTION) {
                        minCoverageRatio = 0.80
                    }
                }
            }
        }
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --diff-file changes.diff \
      --engine JACOCO \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java \
      --min-coverage 0.8 \
      --fail-on-violation \
      --console --html
    ```

    For per-entity thresholds equivalent to the Gradle config, use a config file:

    ```bash
    java -jar delta-coverage-cli.jar --config delta-coverage.yaml
    ```
