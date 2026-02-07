# Legacy Projects

Incrementally improve code coverage in legacy codebases without requiring full project refactoring.

## The Problem

Legacy projects often have low test coverage (10-30%). Requiring 90% coverage immediately is impractical and demoralizing.

## The Solution

Delta Coverage enforces standards only on **new and modified code**. Legacy code remains untouched until you change it.

## Strategy

1. **Set a reasonable threshold** for new code (80-90%)
2. **Don't touch old code** — coverage improves naturally as you modify it
3. **Track progress** with full coverage reports

## Configuration

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith.set("refs/remotes/origin/main")

    reports {
        html.set(true)
        fullCoverageReport.set(true)  // Track overall progress
    }

    reportViews {
        val test by getting {
            violationRules.failIfCoverageLessThan(0.8)  // Start at 80%
        }
    }
}
```

## Phased Approach

### Phase 1: Establish Baseline (Week 1)

Start with a lower threshold:

```kotlin
violationRules.failIfCoverageLessThan(0.6)
```

### Phase 2: Increase Gradually (Month 1-3)

Raise the threshold as the team adapts:

```kotlin
violationRules.failIfCoverageLessThan(0.7)
```

### Phase 3: Target State (Month 3+)

Reach your target:

```kotlin
violationRules.failIfCoverageLessThan(0.9)
```

## Handling Large Legacy Files

When modifying a large legacy file, you might trigger coverage requirements for the entire file. Options:

### Option 1: entityCountThreshold

Ignore violations for small changes:

```kotlin
violationRules {
    rule(CoverageEntity.LINE) {
        minCoverageRatio.set(0.8)
        entityCountThreshold.set(20)  // Only enforce if 20+ lines changed
    }
}
```

### Option 2: Exclude Specific Files

Temporarily exclude problematic files:

```kotlin
excludeClasses.value(
    listOf("**/LegacyMonolith.class")
)
```

### Option 3: Lower View-Specific Threshold

Create a separate view for legacy modules:

```kotlin
reportViews {
    view("legacyModule") {
        includeClasses.value(listOf("**/legacy/**/*.class"))
        violationRules.failIfCoverageLessThan(0.5)
    }

    val test by getting {
        excludeClasses.value(listOf("**/legacy/**/*.class"))
        violationRules.failIfCoverageLessThan(0.9)
    }
}
```

## Tracking Progress

Enable full coverage reports to see overall project coverage:

```kotlin
reports {
    fullCoverageReport.set(true)
}
```

This generates reports in:
- `delta-coverage/` — your changes only
- `full-coverage-report/` — entire project

Track the full coverage percentage over time to see improvement.

## Tips

### Don't Boil the Ocean

Focus on covering new code well. Legacy code coverage will improve naturally.

### Celebrate Wins

When legacy files get updated with tests, acknowledge the improvement.

### Exclude Generated Code

Generated code shouldn't affect your metrics:

```kotlin
excludeClasses.value(
    listOf("**/generated/**/*.*")
)
```

### Use CI Reports

Post coverage comments on PRs to make progress visible:

```yaml
- uses: gw-kit/delta-coverage-action@v1
  with:
    report-path: build/reports/coverage-reports/delta-coverage/test/report.md
```
