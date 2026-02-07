# Local Development

Integrate Delta Coverage into your daily development workflow for instant feedback on test coverage.

## Basic Workflow

1. Write code
2. Write tests
3. Run coverage check
4. Fix gaps, repeat

```bash
./gradlew test deltaCoverage
```

## Recommended Setup

Configure for fast local feedback:

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith.set("refs/remotes/origin/main")

    reports {
        console.set(true)
        html.set(true)
    }

    reportViews {
        val test by getting {
            violationRules.failIfCoverageLessThan(0.8)
        }
    }
}
```

## Quick Commands

### Check coverage of current changes

```bash
./gradlew test deltaCoverage
```

### Compare with different branch

```bash
./gradlew test deltaCoverage -PdiffBase="refs/remotes/origin/develop"
```

### View HTML report

```bash
./gradlew test deltaCoverage
open build/reports/coverage-reports/delta-coverage/test/html/index.html
```

### Debug configuration issues

```bash
./gradlew deltaCoverage -PexplainOnly
```

## IDE Integration

### IntelliJ IDEA

Create a run configuration:

1. **Run → Edit Configurations**
2. **Add New → Gradle**
3. **Tasks**: `test deltaCoverage`
4. **Name**: `Delta Coverage`

Now run coverage with a single click.

### VS Code

Add to `.vscode/tasks.json`:

```json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "Delta Coverage",
            "type": "shell",
            "command": "./gradlew test deltaCoverage",
            "group": "test"
        }
    ]
}
```

## Git Hooks

Run coverage before pushing:

```bash
#!/bin/sh
# .git/hooks/pre-push

./gradlew test deltaCoverage || exit 1
```

Make it executable:

```bash
chmod +x .git/hooks/pre-push
```

## Tips

### Speed Up Feedback

Run only unit tests for faster iteration:

```bash
./gradlew test deltaCoverageTest
```

### Focus on Specific Module

```bash
./gradlew :my-module:test deltaCoverageTest
```

### Fetch Remote Before Running

If comparing with remote branch, fetch first:

```bash
git fetch origin main
./gradlew test deltaCoverage
```

### View Only Changed Classes

The HTML report highlights only changed code. Open it to see exactly which lines need tests.

## Troubleshooting

### Empty Report

- Verify you have uncommitted changes or commits not on main
- Run `git diff origin/main` to see what the plugin will analyze
- Use `-PexplainOnly` to debug

### Wrong Files Analyzed

- Check your diff source configuration
- Ensure remote is fetched: `git fetch origin`
- Verify branch name: `git branch -a`

### Tests Pass but Coverage Fails

The coverage threshold is too high for your current changes. Either:

- Add more tests
- Lower the threshold temporarily
- Use `entityCountThreshold` to ignore small changes
