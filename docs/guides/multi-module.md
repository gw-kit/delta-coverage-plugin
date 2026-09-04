# Multi-Module Projects

Delta Coverage automatically discovers and aggregates coverage across all modules in multi-module Gradle builds.

## How It Works

When applied to the root project, the plugin:

1. Scans all subprojects for test tasks
2. Creates a view for each test task found
3. Creates an `aggregated` view combining all coverage data

## Example Structure

```
my-project/
├── build.gradle.kts       # Apply plugin here
├── app/
│   └── src/test/          # test task → "test" view
├── core/
│   └── src/test/          # test task → "test" view
└── api/
    └── src/test/          # test task → "test" view
```

## Configuration

Apply the plugin to the **root project only**:

```kotlin
// root build.gradle.kts
plugins {
    id("io.github.gw-kit.delta-coverage") version "3.6.1"
}

configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith("refs/remotes/origin/main")

    reportViews {
        val test by getting {
            violationRules.failIfCoverageLessThan(0.9)
        }
    }
}
```

!!! warning "Don't apply to subprojects"
    The plugin should only be applied to the root project. It automatically handles subprojects.

## Aggregated View

When the plugin discovers 2+ views, it creates an `aggregated` view that combines coverage data from all views across all modules:

```bash
./gradlew deltaCoverageAggregated
```

This produces a single report showing coverage across the entire project.

### Configure Aggregated View

```kotlin
reportViews {
    val aggregated by getting {
        violationRules.failIfCoverageLessThan(0.85)
    }
}
```

## Running Coverage Tasks

```bash
# Run all views
./gradlew deltaCoverage

# Run only the aggregated view
./gradlew deltaCoverageAggregated

# Run only the test view (all modules' unit tests)
./gradlew deltaCoverageTest
```

## Multiple Test Types

If modules have different test tasks (e.g., `test`, `integrationTest`):

```
my-project/
├── app/
│   ├── src/test/              # unit tests
│   └── src/integrationTest/   # integration tests
└── core/
    └── src/test/              # unit tests only
```

The plugin creates views for each unique test task:

- `test` - unit tests from all modules
- `integrationTest` - integration tests from `app`
- `aggregated` - everything combined

## Excluding Modules

Exclude a module from coverage:

```kotlin
// In the subproject's build.gradle.kts
tasks.withType<Test> {
    extensions.configure<JacocoTaskExtension> {
        isEnabled = false
    }
}
```

Or exclude specific classes:

```kotlin
// Root build.gradle.kts
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    excludeClasses.value(
        listOf("**/legacy-module/**/*.*")
    )
}
```

## Troubleshooting

### Module coverage not included

1. Verify the module has a `test` task
2. Check that JaCoCo/CoverJet is applied (auto-applied by default)
3. Run with `-PexplainOnly` to see discovered modules

### Aggregated view is missing

The aggregated view is only created when 2+ views exist. If all modules only have the `test` task, there is a single view — declare custom views to get aggregation.

### Different thresholds for different code

Declare custom views with class filters:

```kotlin
reportViews {
    view("coreTests") {
        includeClasses.value(listOf("**/core/**/*.class"))
        violationRules.failIfCoverageLessThan(0.95)  // Stricter
    }

    view("legacyTests") {
        includeClasses.value(listOf("**/legacy/**/*.class"))
        violationRules.failIfCoverageLessThan(0.5)  // More lenient
    }
}
```
