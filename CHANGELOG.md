# Delta-Coverage Gradle plugin Changelog

## [<NEXT-RELEASE>]
### Added
- `Delta Coverage` plugin applies JaCoCo plugin to a project and all it's subprojects
  - could be disabled by adding property to `gradle.properties`:
  ```
  io.github.surpsg.delta-coverage.auto-apply-jacoco=false
  ```
- Support of Gradle configuration cache
- Support of classes exclusion #69

### Changed
- Min supported Gradle is `5.0`.
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
  
### Fixed
