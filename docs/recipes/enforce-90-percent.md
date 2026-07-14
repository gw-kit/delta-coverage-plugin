# Enforce 90% Coverage

Copy-paste configuration to enforce 90% coverage on all new code.

## Basic Configuration

```kotlin
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

## With HTML Reports

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith("refs/remotes/origin/main")

    reports {
        html.set(true)
    }

    reportViews {
        val test by getting {
            violationRules.failIfCoverageLessThan(0.9)
        }
    }
}
```

## With Generated Code Exclusions

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith("refs/remotes/origin/main")

    excludeClasses.value(
        listOf(
            "**/generated/**/*.*",
            "**/*\$\$*.class"
        )
    )

    reportViews {
        val test by getting {
            violationRules.failIfCoverageLessThan(0.9)
        }
    }
}
```

## With Branch Threshold

Branches are often harder to cover. Use a lower threshold with entity count:

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith("refs/remotes/origin/main")

    reportViews {
        val test by getting {
            violationRules {
                failOnViolation.set(true)

                rule(CoverageEntity.LINE) {
                    minCoverageRatio.set(0.9)
                }
                rule(CoverageEntity.INSTRUCTION) {
                    minCoverageRatio.set(0.9)
                }
                rule(CoverageEntity.BRANCH) {
                    minCoverageRatio.set(0.8)
                    entityCountThreshold.set(5)  // Ignore if < 5 branches
                }
            }
        }
    }
}
```

## Run

```bash
./gradlew test deltaCoverage
```

## Expected Output

### Passing

```
+----------------------+----------+----------+--------+
| [test] Delta Coverage Stats                         |
+----------------------+----------+----------+--------+
| Class                | Lines    | Branches | Instr. |
+----------------------+----------+----------+--------|
| com.example.Feature  | 95%      | 90%      | 94%    |
+----------------------+----------+----------+--------+
| Total                | ✔ 95%    | ✔ 90%    | ✔ 94%  |
+----------------------+----------+----------+--------+
| Min expected         | 90%      | 90%      | 90%    |
+----------------------+----------+----------+--------+

BUILD SUCCESSFUL
```

### Failing

```
> Task :deltaCoverage FAILED

Fail on violations: true. Found violations: 1.

> Rule violated for bundle test:
  lines covered ratio is 0.75, but expected minimum is 0.9
```
