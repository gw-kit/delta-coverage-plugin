# Delta-Coverage Gradle plugin Changelog

# 3.2.0

### Changed
- Now lambda coverage is folded into the parent class in textual reports.
- Add `sources` to the `deltaCoverageReport` extension. Now it is possible to override the default source paths.

### Fixed
- #206 Fixed delta coverage input for coverage binaries


# 3.1.1

### Fixed
- Fix aggregated view default enable state when there is only one view is enabled.


## 3.1.0

### New features
- Added extra property `matchClasses` to set patterns of classes for a particular view.
- Added `DeltaCoverageConfiguration.reportViews.<view-name>.enabled` property that allows enabling/disabling a particular view.

### Changed
- Now the `aggregated` view is disabled by default if there is only one view is configured.
- Textual reports now sorted by DESC branches, lines, instructions.
- Reworked GHA PR comment summary report.

### Fixed
- Fix `deltaCoverage*` task creation for custom view.


## 3.0.2

- #180 Markdown and console report now print `no diff` instead of `NaN%`. 


## 3.0.1

- GitHub action now generates the aggregated summary report. 
  It fixes the case when delta coverage check is failed and now summary is published.


## 3.0.0

### New features
- Implemented report views. See [Report views](./README.md#report-views) for details.
  - Each view produces a separate `deltaCoverage<view-name>` task.
- Now `deltaCoverage` is lifecycle task that depends on all `deltaCoverage<view-name>`.
- Reworked [Delta-Coverage GitHub Action](actions/delta-coverage-report/README.md). 
  - Now it publishes Delta-Coverage report using GitHub Check API.
  - PR comment now contains only links to the check runs and short summary.

### Breaking changes
- Id of the plugin was changed to `io.github.gw-kit.delta-coverage`.
- `DeltaCoverageTask` moved to package `io.github.surpsg.deltacoverage.gradle.task`.
- `NativeGitDiffTask` moved to package `io.github.surpsg.deltacoverage.gradle.task`.
- Violation rules are now configured via `DeltaCoverageConfiguration.reportViews.<view-name>.violationRules` extension.
  See [Migration guide](./docs/migration-guilde-v3.md) for details.
- Deleted deprecated CSV report generation.
- Min supported JVM is **17**.
- Min supported Gradle version is **7.6.4**.


## 2.5.0

### Fixed
- #133 Fixed configuration cache compatibility issue.

### Changed
- Min supported Gradle version is **7.0**.


## 2.4.0

### Changed
- Source code paths are inferred from sourceSets if no custom paths are specified.
- `Markdown` and `Console` reports:
  - now don't have `source` column.
  - now contain min coverage data.
  - (console) contain `✔`/`✖` regards to coverage check success or failure.
  - (markdown) contain 🟢/🔴 regards to coverage check success or failure.
- Console report now enabled by the default.

### Added
- Added GitHub Action for posting delta coverage report to PR comment. See [docs](./actions/delta-coverage-report/README.md)


## 2.3.0

### Added
- Added markdown report.

### Dependency updates
- Updated JaCoCo dependency to [0.8.12](https://github.com/jacoco/jacoco/releases/tag/v0.8.12)


## 2.2.1

### Fixed
- Fixed exclude classes ant pattern matching.
- Fixed up-to-date state for diff generation by native git. 
  Now the task is considered not up-to-date if classes have been changed.


## 2.2.0

### Added
- Added native Git support #53.

### Fixed
- Fixed diff generation by JGit client. Now it ignores whitespaces.
- Fixed broken console report layout when class name is too long #92.

### Changed
- HTML report link is printed after console report.


## 2.1.0

### Added
- Render clickable HTML report path in console output.
- Added console report. 

### Fixed
- File changes detecting when there are few classes with similar names in different packages. 


## 2.0.2

### Fixed
- Fixed deltaCoverage task inputs. Now the task is dependent only on required files.
- #46 Fixed build failure when total branches count is 0. 


## 2.0.1
### Fixed
- Fixed build failure when coverage rules are satisfied and coverage engine is INTELLIJ.


## 2.0.0
### Added
- Added support of [Intellij coverage](https://github.com/JetBrains/intellij-coverage).

### Breaking changes
- Base report directory is changed from `build/reports/jacoco/deltaCoverage` to `build/reports/coverage-reports`.
- `jacocoExecFiles` property of `DeltaCoverageConfiguration` extension is renamed to `coverageBinaryFiles`.
- Now auto-applying of JaCoCo(or Kover) plugin is configured via the Delta Coverage plugin extension:
  ```kts
  // kotlin gradle dsl 
  configure<DeltaCoverageConfiguration> {
    coverage {
        engine = CoverageEngine.JACOCO // Default is 'JACOCO'.
        autoApplyPlugin = true // Default is 'true'.
    } 
  }
  ```
  Auto-applying via a project property `io.github.surpsg.delta-coverage.auto-apply-jacoco` is removed.
- Removed deprecated properties from `DeltaCoverageConfiguration` extension:
  - `minLines`
  - `minBranches`
  - `minInstructions`
  See README for the new API.

### Changed
- Min supported Gradle version is **5.6**.
- CSV report generation is deprecated and will be removed in the next major release.

### Dependency updates
- Official support of Gradle **8.5**.


## 1.3.0
### Dependency updates
- Official support of Gradle 8.4
- Updated JaCoCo dependency to [0.8.11](https://github.com/jacoco/jacoco/releases/tag/v0.8.11)


## 1.2.0
- Support java records
### Dependency updates
- Official support of Gradle 8.3


## 1.1.0
### Added
- Ignore coverage violation if a coverage entity count is less than threshold
  <details>
    <summary>Usage example</summary>
        
    ```kts
    // kotlin gradle dsl 
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
      violationRules {
         
          rule(io.github.surpsg.deltacoverage.gradle.CoverageEntity.INSTRUCTION) {
              minCoverageRatio.set(0.9)
              entityCountThreshold.set(1234)
          }
          rule(io.github.surpsg.deltacoverage.gradle.CoverageEntity.LINE) {
              // ... 
          }
          rule(io.github.surpsg.deltacoverage.gradle.CoverageEntity.BRANCH) {
              // ...
          }
  
          // OR configure altogether
          all {
              minCoverageRatio.set(0.6d)
              entityCountThreshold.set(456)
          }  
      }
    }
        
    ```
    </details>
### Dependency updates
- Updated JaCoCo dependency to [0.8.10](https://github.com/jacoco/jacoco/releases/tag/v0.8.10)
- Official support Gradle `7.2.+`.


## 1.0.0
### Added
- `Delta Coverage` plugin applies JaCoCo plugin to a project and all it's subprojects
  - could be disabled by adding property to `gradle.properties`:
  ```
  io.github.surpsg.delta-coverage.auto-apply-jacoco=false
  ```
- Support of Gradle configuration cache
- Support of classes exclusion #69

### Changed
- Official support Gradle `8.1.+`.
- Updated JaCoCo dependency to [0.8.9](https://github.com/jacoco/jacoco/releases/tag/v0.8.9)
- Min supported Gradle is `5.1`.
- `compareWith` and `failIfCoverageLessThan` are changed to infix function: 
  - <details>
    <summary>Usage example</summary>
    
     ```kotlin
    // kotlin gradle dsl 
    configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
        // both are correct
        diffSource.git compareWith "HEAD"
        diffSource.git.compareWith("HEAD")
        
        // both are correct
        violationRules failIfCoverageLessThan 0.7
        violationRules.failIfCoverageLessThan(0.7)
    }
    
    ```
    </details>
- `deltaCoverage` task depends on `classes` tasks.
- Replaced the plugin extension values with [properties](https://docs.gradle.org/current/userguide/lazy_configuration.html)
