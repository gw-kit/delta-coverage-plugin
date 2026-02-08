# Exclude Classes

Exclude classes from coverage analysis using glob patterns.

## Global Exclusions

Exclude classes from all views:

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        excludeClasses.value(
            listOf(
                "**/generated/**/*.*",
                "**/*\$\$*.class"
            )
        )
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        excludeClasses = [
            '**/generated/**/*.*',
            '**/*$$*.class'
        ]
    }
    ```

=== "CLI"

    ```bash
    java -jar delta-coverage-cli.jar \
      --exclude '**/generated/**/*.*' \
      --exclude '**/*$$*.class' \
      --diff-file changes.diff \
      --engine JACOCO \
      --coverage-binary build/jacoco/test.exec \
      --classes build/classes/java/main \
      --sources src/main/java \
      --console
    ```

## View-Specific Exclusions

Exclude classes from a specific view only:

```kotlin
reportViews {
    val test by getting {
        excludeClasses.value(
            listOf("**/test/**/*.*")
        )
    }
}
```

View-specific exclusions are combined with global exclusions.

!!! info "CLI"
    The CLI does not have separate views for exclusions. All `--exclude` patterns apply globally. Use a [configuration file](../reference/cli.md#configuration-file) for more complex setups.

## Pattern Syntax

Patterns follow [Gradle's PatternFilterable](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/util/PatternFilterable.html) syntax:

| Pattern | Matches |
|---------|---------|
| `*` | Any characters except `/` |
| `**` | Any characters including `/` |
| `?` | Any single character |

## Common Patterns

### Exclude a Specific Class

```kotlin
"*/com/example/ExcludeMe.class"
```

### Exclude Nested Classes

```kotlin
"**/MyClass\$*.class"  // All nested classes of MyClass
"**/MyClass\$Inner.class"  // Specific nested class
```

### Exclude a Package

```kotlin
"**/com/example/generated/**/*.*"
```

### Exclude Generated Code

```kotlin
listOf(
    "**/generated/**/*.*",
    "**/build/generated/**/*.*",
    "**/*_Generated.class",
    "**/*\$\$*.class"  // Synthetic/proxy classes
)
```

### Exclude Kotlin Metadata

```kotlin
listOf(
    "**/*\$DefaultImpls.class",
    "**/*\$Companion.class"
)
```

### Exclude Android Generated

```kotlin
listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.class",
    "**/Manifest*.*",
    "**/*_ViewBinding.class"
)
```

## Include Classes

Include only specific classes (whitelist approach):

```kotlin
reportViews {
    val test by getting {
        includeClasses.value(
            listOf("**/com/example/core/**/*.class")
        )
    }
}
```

When `includeClasses` is set, only matching classes are analyzed.

## Full Example

=== "Kotlin DSL"

    ```kotlin
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        diffSource.git.compareWith.set("refs/remotes/origin/main")

        // Global exclusions
        excludeClasses.value(
            listOf(
                "**/generated/**/*.*",
                "**/*\$\$*.class",
                "**/BuildConfig.class"
            )
        )

        reportViews {
            val test by getting {
                // Additional exclusions for this view
                excludeClasses.value(
                    listOf("**/test/fixtures/**/*.*")
                )

                violationRules.failIfCoverageLessThan(0.9)
            }

            val integrationTest by getting {
                // Only analyze API classes
                includeClasses.value(
                    listOf("**/api/**/*.class")
                )
            }
        }
    }
    ```

=== "Groovy DSL"

    ```groovy
    deltaCoverageReport {
        diffSource.git.compareWith = 'refs/remotes/origin/main'

        excludeClasses = [
            '**/generated/**/*.*',
            '**/*$$*.class',
            '**/BuildConfig.class'
        ]

        reportViews {
            test {
                excludeClasses = ['**/test/fixtures/**/*.*']
                violationRules.failIfCoverageLessThan(0.9)
            }

            integrationTest {
                includeClasses = ['**/api/**/*.class']
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
      --exclude '**/generated/**/*.*' \
      --exclude '**/*$$*.class' \
      --exclude '**/BuildConfig.class' \
      --min-coverage 0.9 \
      --fail-on-violation \
      --console
    ```

## Debugging Exclusions

Use the explain report to see which classes are included/excluded:

```bash
./gradlew deltaCoverage -PexplainOnly
```

The report shows resolved class directories and applied filters.
