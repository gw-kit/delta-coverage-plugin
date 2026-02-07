# Report Views

Views let you configure different coverage reports and thresholds for different test tasks.

!!! info "CLI"
    Report views are a Gradle plugin concept. The CLI produces a single coverage report per run. Use `--view-name` to set the view name in the report output. To analyze different test types separately, run the CLI multiple times with different `--coverage-binary` inputs.

## Auto-Discovery

The plugin automatically discovers test tasks and creates views:

| Test Task | View Name | Description |
|-----------|-----------|-------------|
| `test` | `test` | Unit tests |
| `integrationTest` | `integrationTest` | Integration tests |
| `functionalTest` | `functionalTest` | Functional tests |
| (multiple) | `aggregated` | Merged coverage from all tasks |

!!! note "Aggregated view"
    The `aggregated` view is only created if there are 2+ test tasks.

## Configuring Views

Configure an auto-discovered view:

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    reportViews {
        val test by getting {
            violationRules.failIfCoverageLessThan(0.9)
        }
    }
}
```

## Creating Custom Views

Custom views let you enforce coverage rules for specific classes using specific test types.

### Why Custom Views?

Auto-discovered views check coverage across all classes. But sometimes you need:

- **Repositories** covered by integration tests (not unit tests with mocks)
- **Controllers** covered by E2E tests
- **Core domain** covered by unit tests only
- **Different thresholds** for different architectural layers

### Basic Syntax

```kotlin
reportViews {
    val myView by creating {
        // Configuration
    }
}
```

### Example: Repository Coverage with Integration Tests

Ensure repository classes are tested against a real database:

```kotlin
reportViews {
    val repository by creating {
        // Only include repository classes
        includeClasses.value(listOf(
            "**/infrastructure/**/*Repository*"
        ))

        // Use coverage from integration tests only
        coverageBinaryFiles = project(":integration-tests")
            .layout.buildDirectory.asFileTree.matching {
                include("**/jacoco/*.exec")
            }

        // Enforce 80% coverage
        violationRules.failIfCoverageLessThan(0.8)
    }
}
```

This ensures:

- Repository classes have **real database tests**, not mocked unit tests
- Coverage from unit tests (with mocked DB) **doesn't count**
- Build fails if repositories aren't properly integration-tested

### Example: Controller E2E Coverage

```kotlin
reportViews {
    val controllers by creating {
        includeClasses.value(listOf("**/controller/**/*Controller*"))
        coverageBinaryFiles = files("build/jacoco/e2eTest.exec")
        violationRules.failIfCoverageLessThan(0.7)
    }
}
```

## Disabling Views

Disable a view to skip its report:

```kotlin
reportViews {
    val integrationTest by getting {
        enabled.set(false)
    }
}
```

## View-Specific Configuration

Each view can have its own:

### Coverage Binary Files

```kotlin
view("custom") {
    coverageBinaryFiles = files(
        "build/jacoco/test.exec",
        "build/jacoco/integrationTest.exec"
    )
}
```

### Class Filters

```kotlin
view("apiTests") {
    // Only include API classes
    includeClasses.value(listOf("**/api/**/*.class"))

    // Exclude generated code
    excludeClasses.value(listOf("**/generated/**/*.class"))
}
```

### Violation Rules

```kotlin
view("unitTests") {
    violationRules {
        failIfCoverageLessThan(0.9)
    }
}

view("integrationTests") {
    violationRules {
        failIfCoverageLessThan(0.7)  // Different threshold
    }
}
```

## Tasks

Each view creates a Gradle task:

| View | Task |
|------|------|
| `test` | `deltaCoverageTest` |
| `integrationTest` | `deltaCoverageIntegrationTest` |
| `aggregated` | `deltaCoverageAggregated` |

Run a specific view:

```bash
./gradlew deltaCoverageTest
```

Run all views:

```bash
./gradlew deltaCoverage
```

## Example: Testing Pyramid

Configure different thresholds per test type:

```kotlin
reportViews {
    val test by getting {
        violationRules.failIfCoverageLessThan(0.9)  // 90% for unit
    }

    val integrationTest by getting {
        violationRules.failIfCoverageLessThan(0.7)  // 70% for integration
    }

    val e2eTest by getting {
        violationRules.failIfCoverageLessThan(0.5)  // 50% for E2E
    }
}
```
