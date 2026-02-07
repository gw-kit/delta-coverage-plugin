# FAQ

## General

### What is Delta Coverage?

Delta Coverage is a tool that computes code coverage for **new and modified code** based on a git diff. Instead of measuring coverage of the entire codebase, it focuses on what you've changed. It's available as a [Gradle plugin](getting-started/installation.md) and a standalone [CLI tool](reference/cli.md).

### Why use Delta Coverage instead of regular coverage?

- **Actionable feedback** — See exactly what you need to test
- **Fair metrics** — Developers are responsible for their own code, not legacy
- **Incremental improvement** — Coverage improves naturally as code is modified
- **Faster feedback** — Focus on relevant changes, not the entire project

### Does it replace JaCoCo?

No. Delta Coverage uses JaCoCo (or IntelliJ coverage) to collect coverage data. It then filters the data to show only changed code.

### When should I use the CLI instead of the Gradle plugin?

Use the CLI when:

- You cannot modify the project's build scripts to add the Gradle plugin
- You want to run delta coverage as a separate CI step, decoupled from the build
- Your project uses a build tool other than Gradle (Maven, Bazel, etc.) but produces JaCoCo-compatible coverage data

See the [CLI Reference](reference/cli.md) for full details.

## Configuration

### Why isn't the plugin finding my tests?

The plugin auto-discovers test tasks. Ensure:

1. Your test task extends Gradle's `Test` type
2. JaCoCo or CoverJet is applied (auto-applied by default)
3. Tests produce coverage binary files

Debug with:
```bash
./gradlew deltaCoverage -PexplainOnly
```

### How do I compare with a different branch?

```kotlin
diffSource.git.compareWith.set("refs/remotes/origin/develop")
```

Or via command line:
```bash
./gradlew deltaCoverage -PdiffBase="refs/remotes/origin/develop"
```

!!! info "CLI"
    With the CLI, generate the diff externally:
    ```bash
    git diff origin/develop...HEAD > changes.diff
    java -jar delta-coverage-cli.jar --diff-file changes.diff ...
    ```

### Why is my coverage report empty?

Common causes:

1. **No changes** — You have no diff compared to the base branch
2. **Wrong base branch** — Verify you're comparing with the right branch
3. **Tests didn't run** — Run tests before `deltaCoverage`
4. **Source paths don't match** — Diff file paths must match source directories

Debug:
```bash
git diff origin/main --name-only  # See what files changed
./gradlew deltaCoverage -PexplainOnly  # See what the plugin detected
```

!!! info "CLI"
    For the CLI, ensure `--classes` and `--sources` paths match the structure in your diff file. Use `--verbose` for detailed output.

### Can I use Delta Coverage with Kotlin?

Yes. For best results with Kotlin, use the IntelliJ coverage engine:

```kotlin
coverage {
    engine = CoverageEngine.INTELLIJ
}
```

With the CLI: `java -jar delta-coverage-cli.jar --engine INTELLIJ ...`

## Thresholds

### What's a good coverage threshold?

Start with 80% and adjust based on your team:

- **80%** — Good starting point for most teams
- **90%** — Mature teams with good testing culture
- **70%** — Legacy projects or teams new to testing

### How do I handle branches that are hard to cover?

Use `entityCountThreshold` to ignore small changes:

```kotlin
rule(CoverageEntity.BRANCH) {
    minCoverageRatio.set(0.8)
    entityCountThreshold.set(5)  // Ignore if < 5 branches
}
```

### Can I have different thresholds for different modules?

Yes, use views with class filters:

```kotlin
view("core") {
    includeClasses.value(listOf("**/core/**/*.class"))
    violationRules.failIfCoverageLessThan(0.95)
}

view("legacy") {
    includeClasses.value(listOf("**/legacy/**/*.class"))
    violationRules.failIfCoverageLessThan(0.5)
}
```

## CI/CD

### Why does it fail in CI but pass locally?

Common causes:

1. **Stale coverage data** — Run `./gradlew clean test deltaCoverage`. Coverage data is appended to existing files, so old data may not reflect current code/test state
2. **Different base branches** — CI might compare with `main`, local with `develop`
3. **Shallow clone** — Ensure `fetch-depth: 0` in your checkout action
4. **Stricter thresholds** — Check if CI has different configuration

### How do I post coverage to PR comments?

Use the [delta-coverage-action](https://github.com/gw-kit/delta-coverage-action):

```yaml
- uses: gw-kit/delta-coverage-action@v1
  with:
    report-path: build/reports/coverage-reports/delta-coverage/test/report.md
```

## Troubleshooting

### Error: "No coverage data found"

1. Run tests before `deltaCoverage`: `./gradlew test deltaCoverage`
2. Verify coverage binary files exist: `find build -name "*.exec"`
3. Check the explain report: `./gradlew deltaCoverage -PexplainOnly`

!!! info "CLI"
    Verify that `--coverage-binary` points to existing files. Use glob patterns like `'build/**/jacoco/*.exec'` for automatic discovery.

### Error: "Cannot compute diff"

1. Ensure you're in a git repository
2. Fetch the remote branch: `git fetch origin main`
3. Verify the branch exists: `git branch -a | grep main`

!!! info "CLI"
    The CLI reads a diff file directly and does not compute diffs from git. Ensure `--diff-file` points to a valid unified diff file.

### Error: "Plugin must be applied to root project"

Move the plugin application to your root `build.gradle.kts`, not a subproject. This error does not apply to the CLI.

## Getting Help

- [GitHub Issues](https://github.com/gw-kit/delta-coverage-plugin/issues) — Report bugs and request features
- [GitHub Discussions](https://github.com/gw-kit/delta-coverage-plugin/discussions) — Ask questions
