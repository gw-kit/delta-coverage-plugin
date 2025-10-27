Update the Gradle wrapper to the latest version and update all related documentation and tests.

## Steps to perform:

1. **Update Gradle wrapper** to the latest version:
   - Run: `./gradlew wrapper --gradle-version=latest && ./gradlew wrapper`
   - Note the new Gradle version that was installed

2. **Check supported Gradle major versions**:
   - Identify all major versions currently supported (e.g., 7.x, 8.x, 9.x)
   - For functional tests, we need the latest patch version of each supported major version
   - Check if there's a release candidate (RC) version greater than the latest stable version

3. **Update functional tests** in `delta-coverage-gradle/src/functionalTest/kotlin/io/github/surpsg/deltacoverage/gradle/DeltaCoverageGradleReleasesTest.kt`:
   - Update the `@ValueSource` strings array to include:
     - The minimum supported version (keep 7.6.4 or update if min version changed)
     - The latest patch release of each supported major version (e.g., latest 8.x)
     - The newest stable version just installed
     - **If applicable**: The latest RC version (if it's greater than the latest stable version)
   - Example format with RC version:
     ```kotlin
     @ValueSource(
         strings = [
             "7.6.4",      // minimum supported
             "8.14.3",     // latest 8.x
             "9.1.0",      // latest stable version
             "9.2.0-rc-1", // latest RC (if greater than stable)
         ]
     )
     ```
   - Example format without RC version (when RC is not available or not newer):
     ```kotlin
     @ValueSource(
         strings = [
             "7.6.4",      // minimum supported
             "8.14.3",     // latest 8.x
             "9.1.0",      // latest stable version
         ]
     )
     ```

4. **Update CHANGELOG.md**:
   - Add or update the latest version section
   - Under "Dependency updates" section, add:
     ```markdown
     - Updated Gradle to [X.Y.Z](https://github.com/gradle/gradle/releases/tag/vX.Y.Z).
     ```

5. **Update README.md compatibility table**:
   - Locate the "Compatibility" section with the table
   - Update the Gradle version range for the current plugin version (3.+)
   - Format: `**7.6.4** - **X.Y.+**` where X.Y is the new major.minor version
   - Example:
     ```markdown
     | Delta Coverage plugin | Gradle                 | min JVM |
     |-----------------------|------------------------|---------|
     | **3.+**               | **7.6.4** - **9.1.+**  | 17      |
     ```

6. **Verify the changes**:
   - Run functional tests: `./gradlew :delta-coverage-gradle:functionalTest --tests "DeltaCoverageGradleReleasesTest"`
   - Ensure all Gradle versions in the test pass

## Important notes:
- Include comments in the test file to indicate which version is which (min, latest 8.x, latest stable, latest RC)
- Keep the test versions sorted in ascending order
- Only include RC versions if they are greater than the latest stable version
- Update the max version in README to use `.+` suffix based on the latest **stable** version (e.g., `9.1.+` not `9.1.0`), not the RC version
- Make sure to check Gradle release notes for any breaking changes