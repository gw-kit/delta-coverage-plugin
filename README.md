# Delta Coverage gradle plugin

![GitHub Release](https://img.shields.io/github/v/release/SurpSG/delta-coverage-plugin)
[![Main branch checks](https://github.com/SurpSG/delta-coverage-plugin/actions/workflows/main-branch.yml/badge.svg?branch=main)](https://github.com/SurpSG/delta-coverage-plugin/actions/workflows/main-branch.yml)
[![codecov](https://codecov.io/gh/SurpSG/delta-coverage-plugin/branch/main/graph/badge.svg?token=69BAXyEhse)](https://codecov.io/gh/SurpSG/delta-coverage-plugin)
[![GitHub issues](https://img.shields.io/github/issues/SurpSG/delta-coverage-plugin)](https://github.com/SurpSG/delta-coverage-plugin/issues)
[![GitHub stars](https://img.shields.io/github/stars/SurpSG/delta-coverage-plugin?style=flat-square)](https://github.com/SurpSG/delta-coverage-plugin/stargazers)

`Delta Coverage` is coverage analyzing tool that computes code coverage of new/modified code based on a
provided [diff](https://en.wikipedia.org/wiki/Diff#Unified_format).
The diff content can be provided via path to patch file, URL or using embedded git(
see [parameters description](#Parameters-description)).

Why should I use it?

* forces each developer to be responsible for its own code quality(see [deltaCoverage task](#gradle-task-description))
* helps to increase total code coverage(especially useful for old legacy projects)
* reduces time of code review(you don't need to waste your time to track what code is covered)

## Installation

### Compatibility

Delta Coverage plugin compatibility table:

| Delta Coverage plugin | Gradle                 |
|-----------------------|------------------------|
| **3.+**               | **6.7.1** - **8.10.+** |
| **2.5.+**             | **6.7.1** - **8.10.2** |
| **2.0.+** - **2.4.0** | **5.6** - **8.9.+**    |
| **1.3.+**             | **5.1** - **8.4.+**    |
| **1.0.0** - **1.2.0** | **5.1** - **8.3.+**    |

### Apply `Delta Coverage` plugin

The plugin should be applied to the **root** project.

<details open>

<summary><b>Kotlin</b></summary>

```kotlin
plugins {
    id("io.github.surpsg.delta-coverage") version "<the-plugin-version>"
}
```

The latest release version is ![GitHub Release](https://img.shields.io/github/v/release/SurpSG/delta-coverage-plugin)

</details>

<details>

<summary><b>Groovy</b></summary>

```groovy
plugins {
    id "io.github.surpsg.delta-coverage" version "<the-plugin-version>"
}
```

</details>

## Coverage engine

Delta Coverage plugin doesn't collect coverage data itself.
It uses coverage data that is already collected by a coverage engine.

The plugin supports two coverage engines:

- [JaCoCo](https://github.com/jacoco/jacoco) is standard coverage engine for JVM projects.
- [Intellij coverage](https://github.com/JetBrains/intellij-coverage) is coverage engine that used by default in
  Intellij IDE.
  Intellij coverage could be applied to your Gradle project by applying [Kover](https://github.com/Kotlin/kotlinx-kover)
  plugin.
  Intellij coverage is better choice for **Kotlin** projects.

See [Configuration](#configuration) section to configure coverage engine.

## Configuration

In the examples below the minimal configuration is shown.

<details open>
<summary><b>Kotlin</b></summary>

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.file.set("${PATH_TO_DIFF_FILE}")

    reportViews {
        val test by getting {
            violationRules.failIfCoverageLessThan(0.9)
        }
    }
    reports {
        html.set(true)
    }
}
```

</details>

<details>
<summary><b>Groovy</b></summary>

```groovy
deltaCoverageReport {
    diffSource.file = file("${PATH_TO_DIFF_FILE}")

    reportViews {
        test {
            violationRules.failIfCoverageLessThan 0.9d
        }
    }
    reports {
        html.set(true)
    }
}
```

</details>

### Report Views

The concept of views is used to configure different coverage reports for different test tasks. 
So, you can check coverage for different test tasks separately.

#### Auto-configuration

Each view could have its own coverage binary files and violation rules.

Suppose you have a test task `test` and test task `integrationTest` in your project. 
The plugin will automatically register and configures the next views:
- `test` - for task `test`.
- `integrationTest` - for task `integrationTest`.
- `aggregated` - merged coverage data from all test tasks.

## Execute

```shell
./gradlew test deltaCoverage
```

## Parameters description

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    // Configures coverage engine. Default is 'JACOCO'.
    coverage {
        engine = CoverageEngine.JACOCO // Required. Default is 'JACOCO'. Could be set to INTELLIJ engine.
        autoApplyPlugin = true // Required. Default is 'true'. If 'true' then the corresponding coverage engine plugin is applied to a project and all it's subprojects.
    }

    // Required. Only one of `file`, `url` or git must be specified.
    diffSource {
        //  Path to diff file.
        file.set(file("path/to/file.diff"))

        // URL to retrieve diff by.
        url.set("http://domain.com/file.diff")

        // Compares current HEAD and all uncommited with provided branch, revision or tag.
        git.compareWith.set("refs/remotes/origin/develop")
        git.useNativeGit.set(true)
        // Optional. Default is 'false'. If 'true' then the plugin uses native git to get diff.
    }

    // Required. By default sources are taken from jacoco plugin(or intellij) if the plugin is applied to a project.
    srcDirs = files("/path/to/sources")

    // Required. By default classes are taken from jacoco plugin(or intellij) if the plugin is applied to a project.
    classesDirs = files("/path/to/compiled/classes")

    // Optional. Excludes classes from coverage report by set of patterns .
    excludeClasses.value(listOf(
            "*/com/package/ExcludeClass.class", // Excludes class "com.package.ExcludeClass"
            "**/com/package/**/ExcludeClass.class", // Excludes classes like "com.package.ExcludeClass", "com.package.sub1.sub2.ExcludeClass", etc.
            "**/ExcludeClass\$NestedClass.class", // Excludes nested class(es) "<any-package>.ExcludeClass.NestedClass"
            "**/com/package/exclude/**/*.*" // Excludes all in package "com.package.exclude"
            // See more info about pattern rules: https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/util/PatternFilterable.html
    ))

    reportViews {
        val test by getting { // Configuring existing report view 'test'.  
          failIfCoverageLessThan(0.9)
        }
      
        view("customView") { // Registering a custom report view.
            // Required. 
            // For JaCoCo engine: by default '.exec' coverage binary files are configured from jacoco plugin.
            // For Intellij engine: by default '.ic' coverage binary files are configured from kover plugin.
            coverageBinaryFiles = files("/path/to/jacoco/exec/file.exec")
            
            // If violation rules are not configured, then no violations will be checked.
            violationRules {
                failOnViolation.set(true) // Optional. Default `false`. If `true` then task will fail if any violation is found.
    
                // [Option 1]---------------------------------------------------------------------------------------------------
                // Optional. The function sets min coverage ration for instructions, branches and lines to '0.9'. 
                // Sets failOnViolation to 'true'.
                failIfCoverageLessThan(0.9)
    
                // [Option 2]---------------------------------------------------------------------------------------------------
                rule(io.github.surpsg.deltacoverage.gradle.CoverageEntity.INSTRUCTION) {
                  // Optional. If coverage ration is set then the plugin will check coverage ratio for this entity.
                  minCoverageRatio.set(0.9)
                  // Optional. Disabled by default. The plugin ignores violation if the entity count is less than the threshold.
                  entityCountThreshold.set(1234)
                }
                rule(io.github.surpsg.deltacoverage.gradle.CoverageEntity.LINE) {
                  // ...
                }
                rule(io.github.surpsg.deltacoverage.gradle.CoverageEntity.BRANCH) {
                  // ...
                }
    
                // [Option 3]---------------------------------------------------------------------------------------------------
                // [.kts only] Alternative way to set violation rule.
                io.github.surpsg.deltacoverage.gradle.CoverageEntity.BRANCH {
                  minCoverageRatio.set(0.7)
                  entityCountThreshold.set(890)
                }
    
                // [Option 4]---------------------------------------------------------------------------------------------------
                // Sets violation rule for all entities: LINE, BRANCH, INSTRUCTION
                all {
                  minCoverageRatio.set(0.7)
                  entityCountThreshold.set(890)
                }
            }
        }
    }
}
```

## Gradle task description

The plugin adds a task `deltaCoverage` that depends on compile tasks.

The task does the following:
* loads code coverage data specified by `deltaCoverageReport.<report-view>.coverageBinaryFiles` for all views.
* analyzes the coverage data and filters according to `diffSource.url`/`diffSource.file`.
* generates html report(if enabled: `reports.html = true`) to directory `reports.baseReportsDir`.
* checks coverage ratio if `<report-view>.violationRules` is specified.

  Violations check is enabled only if there is any rule for `INSTRUCTION`, `LINE`, `BRANCH` has min ratio greater
  than `0.0`.

  Fails the execution if the violation check is enabled and `violationRules.failOnViolation = true`.

## Violations check output example

Passed:
> \>Task :deltaCoverage
>
> Fail on violations: true. Found violations: 0.

Failed:
> \> Task :deltaCoverage FAILED
>
>Fail on violations: true. Found violations: 2.
>
>FAILURE: Build failed with an exception.
>
>...
>
>\> java.lang.Exception: Rule violated for bundle test: instructions covered ratio is 0.5, but expected
> minimum is 0.9
>
> [test] Rule violated for bundle test: lines covered ratio is 0.0, but expected minimum is 0.9

## Delta Coverage report examples

### HTML report

`Delta Coverage` plugin generates standard JaCoCo HTML report, but highlights only modified code

<img src="https://user-images.githubusercontent.com/8483470/77781538-a74f3480-704d-11ea-9e39-051f1001b88a.png" width=500  alt="DeltaCoverage HTML report"/>

<details>
  <summary><b>JaCoCo HTML report</b></summary> 
  <img src="https://user-images.githubusercontent.com/8483470/77781534-a61e0780-704d-11ea-871e-879fb45757cd.png" width=500 alt="JaCoCo HTML report"/>        
</details>

### Console report

The report is printed to the console:

```
+----------------------+----------+----------+--------+
| [test] Delta Coverage Stats                         |
+----------------------+----------+----------+--------+
| Class                | Lines    | Branches | Instr. |
+----------------------+----------+----------+--------|
| com.java.test.Class1 | 66.67%   | 50%      | 65%    |
+----------------------+----------+----------+--------+
| Total                | ✔ 66.67% | ✖ 50%    | ✔ 65%  |
+----------------------+----------+----------+--------+
| Min expected         | 66%      | 60%      | 60%    |
+----------------------+----------+----------+--------+
```

### Markdown report

The report is saved to the file `build/reports/coverage-reports/delta-coverage/test/report.md`.

| Class        | Lines     | Branches | Instr.    |
|--------------|-----------|----------|-----------|
| class2       | 87.50%    | 83.33%   | 90%       |
| class1       | 75%       | 50%      | 83.33%    |
| Total        | 🔴 83.33% | 🟢 75%   | 🔴 87.50% |
| Min expected | 90%       | 75%      | 95%       |
