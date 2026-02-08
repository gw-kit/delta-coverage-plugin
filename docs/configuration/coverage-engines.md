# Coverage Engines

Delta Coverage supports two coverage engines. The plugin doesn't collect coverage itself — it uses data from these engines.

## JaCoCo (Default)

The standard coverage engine for JVM projects. Works well with Java and most Kotlin code.

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        coverage {
            engine = CoverageEngine.JACOCO
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        coverage {
            engine = 'JACOCO'
        }
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --engine JACOCO \
      --diff-file changes.diff \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java \
      --console
    ```

- Produces `.exec` binary files
- Widely supported and documented
- Default choice for most projects

## IntelliJ Coverage

Better accuracy for Kotlin projects, especially with inline functions and coroutines.

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        coverage {
            engine = CoverageEngine.INTELLIJ
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        coverage {
            engine = 'INTELLIJ'
        }
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --engine INTELLIJ \
      --diff-file changes.diff \
      --coverage-binary build/coverage/test.ic \
      --classes build/classes/kotlin/main \
      --sources src/main/kotlin \
      --console
    ```

- Produces `.ic` binary files
- Uses [CoverJet plugin](https://github.com/gw-kit/cover-jet-plugin) under the hood
- Recommended for Kotlin-heavy projects

!!! tip "When to use IntelliJ engine"
    If you see incorrect coverage for Kotlin inline functions, suspend functions, or coroutines, switch to IntelliJ engine.

## Auto-Apply Behavior

By default, the Gradle plugin auto-applies the coverage engine plugin:

```kotlin
coverage {
    autoApplyPlugin = true  // default
}
```

This means:

- **JaCoCo engine**: Applies `jacoco` plugin to all projects
- **IntelliJ engine**: Applies `cover-jet` plugin to all projects

To disable auto-apply (if you configure coverage manually):

```kotlin
coverage {
    autoApplyPlugin = false
}
```

!!! info "CLI"
    Auto-apply is a Gradle plugin feature. The CLI requires you to provide pre-generated coverage binary files via `--coverage-binary`.

## Comparison

| Feature | JaCoCo | IntelliJ |
|---------|--------|----------|
| Java accuracy | Excellent | Excellent |
| Kotlin accuracy | Good | Excellent |
| Inline functions | Partial | Full |
| Coroutines | Partial | Full |
| Binary format | `.exec` | `.ic` |
| Ecosystem support | Wide | Growing |

## Mixing Engines

You cannot mix engines in a single build. Choose one engine for your entire project.

## Troubleshooting

### No coverage data found

1. Verify tests ran before `deltaCoverage`
2. Check binary files exist (`build/jacoco/*.exec` or `build/coverage/*.ic`)
3. Run with `-PexplainOnly` to see detected files

### Wrong coverage for Kotlin

Switch to IntelliJ engine:

=== "Kotlin DSL"

    ```kotlin
    coverage {
        engine = CoverageEngine.INTELLIJ
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        coverage {
            engine = 'INTELLIJ'
        }
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar --engine INTELLIJ ...
    ```
