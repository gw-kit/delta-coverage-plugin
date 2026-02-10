# Kotlin Projects

For Kotlin projects, the IntelliJ coverage engine provides better accuracy than JaCoCo, especially for inline functions, coroutines, and suspend functions.

## Why IntelliJ Coverage for Kotlin?

JaCoCo instruments bytecode, which can miss or misreport coverage for Kotlin-specific features:

| Feature | JaCoCo | IntelliJ |
|---------|--------|----------|
| Inline functions | Often incorrect | Accurate |
| Suspend functions | Partial | Accurate |
| Coroutines | Partial | Accurate |
| Default parameters | Sometimes incorrect | Accurate |
| Companion objects | Accurate | Accurate |

## Configuration

Switch to IntelliJ coverage engine:

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    coverage {
        engine = CoverageEngine.INTELLIJ
    }

    diffSource.git.compareWith("refs/remotes/origin/main")
}
```

The plugin automatically applies the [CoverJet plugin](https://github.com/gw-kit/cover-jet-plugin) which uses IntelliJ coverage under the hood.

## CoverJet Plugin

CoverJet is a Gradle plugin that integrates IntelliJ coverage into Gradle builds. When you set `engine = CoverageEngine.INTELLIJ`, Delta Coverage:

1. Applies CoverJet plugin to all projects
2. Configures test tasks to collect IntelliJ coverage
3. Reads `.ic` coverage files instead of `.exec`

### Manual CoverJet Setup

If you need custom CoverJet configuration:

```kotlin
// build.gradle.kts
plugins {
    id("io.github.gw-kit.cover-jet") version "1.0.0"
}

configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    coverage {
        engine = CoverageEngine.INTELLIJ
        autoApplyPlugin = false  // Don't auto-apply, we did it manually
    }
}
```

## Mixed Java/Kotlin Projects

IntelliJ coverage works well for mixed projects. It provides accurate coverage for both Java and Kotlin code.

```kotlin
coverage {
    engine = CoverageEngine.INTELLIJ  // Works for Java too
}
```

## Excluding Kotlin-Generated Code

Exclude Kotlin metadata and synthetic classes:

```kotlin
excludeClasses.value(
    listOf(
        "**/*\$DefaultImpls.class",
        "**/*\$Companion.class",
        "**/*\$WhenMappings.class"
    )
)
```

## Troubleshooting

### Coverage shows 0% for inline functions

Switch to IntelliJ engine:

```kotlin
coverage {
    engine = CoverageEngine.INTELLIJ
}
```

### "Cannot find CoverJet plugin"

Ensure you have access to the plugin. Add to your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
```

### Coverage binary files not found

Verify test tasks ran and produced `.ic` files:

```bash
find build -name "*.ic"
```

Use the explain report to debug:

```bash
./gradlew deltaCoverage -PexplainOnly
```
