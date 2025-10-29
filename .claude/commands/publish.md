---
description: Publish delta-coverage-core to Maven Central and delta-coverage-gradle to Gradle Plugin Portal
---

# Publish Delta Coverage

You are tasked with publishing Delta Coverage modules:
- `delta-coverage-core` to Maven Central Portal (using vanniktech gradle-maven-publish-plugin)
- `delta-coverage-gradle` to Gradle Plugin Portal (using com.gradle.plugin-publish)

## Steps to complete:

### 1. Verify and update version

1. **Check current version in gradle.properties**
   - Read the `version` property from `gradle.properties`
   - Format should be: `version=x.y.z` (no SNAPSHOT suffix for releases)

2. **Determine if version update is needed**
   - Ask the user what version to publish
   - If publishing a new release, update `gradle.properties` with the new version
   - Remove any SNAPSHOT suffix if present

3. **Verify version consistency**
   - The version in `gradle.properties` should be the version being published
   - Check if this version already exists on Maven Central to avoid conflicts

### 2. Run tests and build

1. **Execute full test suite**
   - Run: `./gradlew :delta-coverage-core:clean :delta-coverage-core:build`
   - Ensure all tests pass
   - If tests fail, STOP and report failures

2. **Verify build artifacts**
   - Check that JARs are created: `ls -lh delta-coverage-core/build/libs/`
   - Should see: main JAR, sources JAR, javadoc JAR

### 3. Test publication locally (optional but recommended)

1. **Publish to Maven Local first**
   - Run: `./gradlew :delta-coverage-core:publishToMavenLocal`
   - This will test signing and POM generation without actually publishing

2. **Verify local publication**
   - Check artifacts in: `~/.m2/repository/io/github/gw-kit/delta-coverage-core/<version>/`
   - Verify all files have `.asc` signatures
   - Verify POM contains correct metadata

3. **Inspect POM file**
   - Check: `cat ~/.m2/repository/io/github/gw-kit/delta-coverage-core/<version>/delta-coverage-core-<version>.pom`
   - Verify developer info, SCM URLs, license, description are correct

### 4. Publish to Maven Central

1. **Choose publication method**

   **Option A: Publish to staging (manual release)**
   - Run: `./gradlew :delta-coverage-core:publishToMavenCentral`
   - This creates a staging deployment that requires manual release via Portal UI

   **Option B: Publish and auto-release (recommended for stable releases)**
   - Run: `./gradlew :delta-coverage-core:publishAndReleaseToMavenCentral`
   - This publishes and automatically releases to Maven Central

2. **Wait for publication to complete**
   - The Gradle task should complete successfully
   - Look for: `> Task :delta-coverage-core:publishMavenPublicationToMavenCentralRepository`

3. **Verify publication**
   - If using Option A (staging):
     - Go to https://central.sonatype.com/
     - Navigate to your deployments
     - Review the uploaded artifacts
     - Click "Publish" to release

   - If using Option B (auto-release):
     - Publication happens automatically
     - No manual action needed in the Portal

### 5. Publish to Gradle Plugin Portal

1. **Build and test the Gradle plugin**
   - Run: `./gradlew :delta-coverage-gradle:build functionalTest`
   - Ensure all tests pass including functional tests
   - If tests fail, STOP and report failures

2. **Test plugin publication locally (optional)**
   - Run: `./gradlew :delta-coverage-gradle:publishToMavenLocal`
   - This tests plugin marker generation and metadata

3. **Publish to Gradle Plugin Portal**
   - Run: `./gradlew :delta-coverage-gradle:publishPlugins`
   - This publishes the plugin and plugin marker artifacts
   - Wait for task completion: `> Task :delta-coverage-gradle:publishPlugins`

4. **Verify publication on Plugin Portal**
   - Wait ~10-15 minutes for processing
   - Visit: https://plugins.gradle.org/plugin/io.github.gw-kit.delta-coverage
   - Verify the new version appears
   - Check that plugin metadata is correct (description, tags, etc.)

### 6. Verify deployments

**Maven Central:**
1. **Check Maven Central search**
   - Wait ~30 minutes for sync
   - Visit: https://central.sonatype.com/artifact/io.github.gw-kit/delta-coverage-core
   - Verify the new version appears

2. **Verify artifact availability**
   - Try downloading: https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-core/<version>/
   - Check all artifacts are available: JAR, sources, javadoc, POM, signatures

**Gradle Plugin Portal:**
1. **Check Plugin Portal**
   - Visit: https://plugins.gradle.org/plugin/io.github.gw-kit.delta-coverage
   - Verify new version is listed
   - Check "How to use" section shows correct version

2. **Test plugin installation**
   - Create a test project
   - Add plugin using new version:
     ```kotlin
     plugins {
       id("io.github.gw-kit.delta-coverage") version "<version>"
     }
     ```
   - Verify plugin applies successfully

### 7. Post-publication tasks

1. **Tag the release in Git** (if this is a release version)
   - Create tag: `git tag v<version>`
   - Push tag: `git push origin v<version>`

2. **Update CHANGELOG.md**
   - Document the published version
   - Add release date

## Important Notes:

- **Version**: Never publish the same version twice - both portals don't allow overwrites
- **Testing**: Always test locally with `publishToMavenLocal` first for both modules
- **Signing**: All artifacts must be GPG signed for Maven Central (configured in `~/.gradle/gradle.properties`)
- **Dependencies**: delta-coverage-gradle depends on delta-coverage-core, so publish core first
- **Publishing order**: Publish core to Maven Central first, then gradle plugin to Plugin Portal
- **Wait times**:
  - Maven Central: ~30 minutes for sync
  - Gradle Plugin Portal: ~10-15 minutes for processing

## Configuration Files:

**delta-coverage-core:**
- `delta-coverage-core/gradle.properties` - POM metadata for Maven Central
- `delta-coverage-core/build.gradle.kts` - vanniktech maven-publish plugin

**delta-coverage-gradle:**
- `delta-coverage-gradle/build.gradle.kts` - Gradle plugin definition
- `buildSrc/src/main/kotlin/gradle-plugin-conventions.gradle.kts` - Plugin publish setup

**Shared:**
- `gradle/deps.versions.toml` - Plugin versions
- `gradle.properties` - Project version (applies to both modules)

## Publishing Tasks Available:

**Maven Central (delta-coverage-core):**
```bash
# Test locally
./gradlew :delta-coverage-core:publishToMavenLocal

# Publish to staging (manual release)
./gradlew :delta-coverage-core:publishToMavenCentral

# Publish and auto-release
./gradlew :delta-coverage-core:publishAndReleaseToMavenCentral
```

**Gradle Plugin Portal (delta-coverage-gradle):**
```bash
# Test locally
./gradlew :delta-coverage-gradle:publishToMavenLocal

# Publish to Plugin Portal
./gradlew :delta-coverage-gradle:publishPlugins
```

**Both (complete release):**
```bash
# Publish both modules in sequence
./gradlew :delta-coverage-core:publishAndReleaseToMavenCentral && \
./gradlew :delta-coverage-gradle:publishPlugins
```

## Troubleshooting:

**Maven Central:**
- **401 Unauthorized**: Check `mavenCentralUsername` and `mavenCentralPassword` in gradle.properties
- **Signing failed**: Verify GPG key credentials (`signingInMemoryKey`, `signingInMemoryKeyId`, `signingInMemoryKeyPassword`)
- **Version already exists**: Cannot republish - increment version number
- **Missing signatures**: Ensure `RELEASE_SIGNING_ENABLED=true` in `delta-coverage-core/gradle.properties`
- **Timeout**: Portal API can be slow - wait and retry

**Gradle Plugin Portal:**
- **401 Unauthorized**: Check `gradle.publish.key` and `gradle.publish.secret` in gradle.properties
- **Invalid plugin ID**: Verify plugin ID matches registered namespace
- **Version already published**: Cannot republish - increment version number
- **Functional tests failed**: Fix tests before publishing
- **Plugin marker issues**: Check plugin definition in `delta-coverage-gradle/build.gradle.kts`

## Error Handling:

- If tests fail, STOP and fix issues
- If signing fails, verify GPG credentials
- If publication fails with 401, check Maven Central credentials
- If version conflict, ask user for new version number
- If unsure about any step, ask the user before proceeding
