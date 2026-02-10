# Diff Sources

Delta Coverage needs to know what code changed. You can provide a diff via git, file, or URL.

## Git (Recommended)

Compare your current branch against another branch, tag, or commit:

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        diffSource {
            git.compareWith("refs/remotes/origin/main")
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        diffSource {
            git.compareWith('refs/remotes/origin/main')
        }
    }
    ```

=== "CLI"

    The CLI does not compute git diffs directly. Generate a diff file first:

    ```bash
    git diff origin/main...HEAD > changes.diff

    java -jar delta-coverage-cli.jar \
      --diff-file changes.diff \
      --engine JACOCO \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java \
      --console
    ```

### Common Git References

| Reference | Description |
|-----------|-------------|
| `refs/remotes/origin/main` | Compare with remote main branch |
| `refs/remotes/origin/develop` | Compare with remote develop branch |
| `refs/heads/main` | Compare with local main branch |
| `HEAD~5` | Compare with 5 commits ago |
| `v1.0.0` | Compare with a tag |

### Native Git vs JGit

By default, the plugin uses JGit (pure Java). To use native git instead:

```kotlin
diffSource {
    git.compareWith("refs/remotes/origin/main")
    git.useNativeGit.set(true)
}
```

Use native git if:

- You have a very large repository
- JGit has compatibility issues with your setup
- You need specific git configuration

!!! info "CLI"
    The CLI always uses an external diff file rather than computing diffs internally. Native Git vs JGit does not apply.

## File

Provide a diff file in unified diff format:

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        diffSource {
            file.set(file("path/to/changes.diff"))
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        diffSource {
            file = file('path/to/changes.diff')
        }
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --diff-file path/to/changes.diff \
      --engine JACOCO \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java \
      --console
    ```

Generate a diff file with git:

```bash
git diff origin/main...HEAD > changes.diff
```

## URL

Fetch a diff from a URL:

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        diffSource {
            url.set("https://github.com/owner/repo/compare/main...feature.diff")
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        diffSource {
            url = 'https://github.com/owner/repo/compare/main...feature.diff'
        }
    }
    ```

=== "CLI"

    The CLI does not support fetching diffs from URLs directly. Download the diff first:

    ```bash
    curl -L -o changes.diff \
      "https://github.com/owner/repo/compare/main...feature.diff"

    java -jar delta-coverage-cli.jar \
      --diff-file changes.diff \
      --engine JACOCO \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java \
      --console
    ```

!!! note "GitHub diff URLs"
    GitHub provides diff URLs in the format: `https://github.com/{owner}/{repo}/compare/{base}...{head}.diff`

## Priority

If multiple sources are configured, priority is: **git > file > url**

Only one source is used per run.

!!! info "CLI"
    The CLI only supports the `--diff-file` option. To use a git or URL diff source, generate the diff file externally and pass it to the CLI.

## Debugging Diff Issues

If coverage reports are empty, the diff might not match your source files. Use the explain report to debug:

```bash
./gradlew deltaCoverage -PexplainOnly
```

This generates a diagnostic report showing exactly what the plugin detected.
