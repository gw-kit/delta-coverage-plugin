# Testing Pyramid

Configure different coverage thresholds for each level of the testing pyramid: unit tests, integration tests, and end-to-end tests.

## The Testing Pyramid

```
        /\
       /  \      E2E Tests (few, slow, expensive)
      /----\
     /      \    Integration Tests (some, medium)
    /--------\
   /          \  Unit Tests (many, fast, cheap)
  --------------
```

Each level has different characteristics:

| Level | Speed | Coverage Target | Why |
|-------|-------|-----------------|-----|
| Unit | Fast | 90%+ | Easy to write, should cover most code |
| Integration | Medium | 70-80% | Test interactions, harder to achieve high coverage |
| E2E | Slow | 50-60% | Focus on critical paths, not coverage |

## Configuration

Configure views for each test level:

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith.set("refs/remotes/origin/main")

    reportViews {
        // Unit tests - highest threshold
        val test by getting {
            violationRules.failIfCoverageLessThan(0.9)
        }

        // Integration tests - medium threshold
        val integrationTest by getting {
            violationRules.failIfCoverageLessThan(0.7)
        }

        // E2E tests - lowest threshold
        val e2eTest by getting {
            violationRules.failIfCoverageLessThan(0.5)
        }
    }
}
```

## Setting Up Test Tasks

### Declaring Test Suites

Use the [JVM Test Suite](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html) plugin to declare additional test types:

```kotlin
// build.gradle.kts
testing {
    suites {
        val integrationTest by registering(JvmTestSuite::class)
        val e2eTest by registering(JvmTestSuite::class)
    }
}
```

Each test suite registers a corresponding test task. Delta Coverage auto-discovers these tasks and creates views for them.

## Running Tests

Run all tests and coverage:

```bash
./gradlew test integrationTest e2eTest deltaCoverage
```

Run specific levels:

```bash
# Only unit tests
./gradlew test deltaCoverageTest

# Only integration tests
./gradlew integrationTest deltaCoverageIntegrationTest
```

## Per-Level Reports

Each view generates its own report:

```
build/reports/coverage-reports/delta-coverage/
├── test/              # Unit test coverage
├── integrationTest/   # Integration test coverage
├── e2eTest/           # E2E test coverage
└── aggregated/        # Combined coverage
```

## Best Practices

### 1. Don't Over-Test at the Wrong Level

High unit test coverage with low integration coverage is fine. Don't force integration tests just to hit coverage numbers.

### 2. Use entityCountThreshold

For integration and E2E tests, small changes might not have many branches to test:

```kotlin
val integrationTest by getting {
    violationRules {
        rule(CoverageEntity.BRANCH) {
            minCoverageRatio.set(0.7)
            entityCountThreshold.set(10)  // Ignore if < 10 branches
        }
    }
}
```

### 3. Focus E2E on Critical Paths

E2E tests should cover critical user journeys, not maximize coverage. Use `includeClasses` to focus:

```kotlin
val e2eTest by getting {
    includeClasses.value(
        listOf("**/api/**/*.class", "**/controller/**/*.class")
    )
    violationRules.failIfCoverageLessThan(0.6)
}
```

### 4. Aggregated View for Overall Health

Use the aggregated view to ensure combined coverage meets minimum standards:

```kotlin
val aggregated by getting {
    violationRules.failIfCoverageLessThan(0.8)
}
```

## Example: Full Configuration

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith.set("refs/remotes/origin/main")

    reports {
        html.set(true)
        markdown.set(true)
    }

    reportViews {
        val test by getting {
            violationRules {
                failIfCoverageLessThan(0.9)
            }
        }

        val integrationTest by getting {
            violationRules {
                rule(CoverageEntity.LINE) {
                    minCoverageRatio.set(0.7)
                }
                rule(CoverageEntity.BRANCH) {
                    minCoverageRatio.set(0.6)
                    entityCountThreshold.set(5)
                }
            }
        }

        val e2eTest by getting {
            violationRules {
                failOnViolation.set(false)  // Don't fail, just report
                all {
                    minCoverageRatio.set(0.5)
                }
            }
        }

        val aggregated by getting {
            violationRules.failIfCoverageLessThan(0.8)
        }
    }
}
```
