---
description: Publish to Maven Central, Gradle Plugin Portal, and create GitHub Release
---

# Publish Delta Coverage

You are tasked with publishing Delta Coverage modules and creating a GitHub release.

This command should be run AFTER:
- Release branch has been prepared (using `/prepare-release`)
- Release PR has been merged to `main` branch
- You are on `main` branch with latest changes

## Prerequisites Verification

Before starting, verify:

1. **Check current branch**
   - Run: `git branch --show-current`
   - Must be on `main` branch
   - If not on main, STOP and inform user

2. **Verify branch is up-to-date**
   - Run: `git fetch origin && git status`
   - Must be up-to-date with origin/main
   - If behind, STOP and ask user to pull latest changes

3. **Read version from gradle.properties**
   - Read `gradle.properties` to get version
   - Version should NOT contain SNAPSHOT
   - If SNAPSHOT found, STOP and inform user

4. **Verify changelog exists**
   - Read CHANGELOG.md
   - Must have section for current version
   - If not found, STOP and inform user

## Steps to complete:

### 1. Extract changelog for current version

1. **Read CHANGELOG.md**
   - Extract the section for current version
   - Format: From `## x.y.z` to the next `## ` heading
   - Store this content for GitHub release notes

2. **Verify changelog content**
   - Ensure it's not empty
   - Should contain meaningful release notes
   - If empty, ask user to provide content

### 2. Run final verification build

1. **Clean build both modules**
   - Run: `./gradlew clean build`
   - This ensures everything builds correctly
   - If build fails, STOP and report errors

2. **Verify test results**
   - All tests must pass
   - Check for any warnings
   - If tests fail, STOP and report failures

### 3. Publish delta-coverage-core to Maven Central

1. **Publish and auto-release to Maven Central**
   - Run: `./gradlew :delta-coverage-core:publishAndReleaseToMavenCentral`
   - This publishes to Maven Central and automatically releases
   - Wait for task completion
   - Look for: `> Task :delta-coverage-core:publishMavenPublicationToMavenCentralRepository`

2. **Handle errors**
   - If 401 Unauthorized: Check `mavenCentralUsername` and `mavenCentralPassword` in gradle.properties
   - If signing fails: Verify GPG credentials
   - If version exists: Version already published, STOP
   - If task fails: Report error to user and STOP

3. **Wait for publication**
   - Task should complete successfully
   - Note: Maven Central sync takes ~30 minutes for public availability

### 4. Publish delta-coverage-gradle to Gradle Plugin Portal

1. **Build and test the Gradle plugin**
   - Run: `./gradlew :delta-coverage-gradle:build functionalTest`
   - Ensure all tests pass including functional tests
   - If tests fail, STOP and report failures

2. **Publish to Gradle Plugin Portal**
   - Run: `./gradlew :delta-coverage-gradle:publishPlugins`
   - Wait for task completion: `> Task :delta-coverage-gradle:publishPlugins`

3. **Handle errors**
   - If 401 Unauthorized: Check `gradle.publish.key` and `gradle.publish.secret`
   - If version exists: Version already published, STOP
   - If task fails: Report error to user and STOP

4. **Wait for publication**
   - Task should complete successfully
   - Note: Plugin Portal processing takes ~10-15 minutes

### 5. Create GitHub Release

1. **Create git tag**
   - Tag format: `v<version>` (e.g., `v3.4.3`)
   - Run: `git tag v<version>`

2. **Push tag to remote**
   - Run: `git push origin v<version>`
   - Verify tag was pushed successfully

3. **Create GitHub release using gh CLI**
   - Use the changelog content extracted in step 1
   - Run: `gh release create v<version> --title "Version <version>" --notes "$(cat <<'EOF'
<changelog content here>
EOF
)"`
   - Example:
     ```bash
     gh release create v3.4.3 --title "Version 3.4.3" --notes "$(cat <<'EOF'
     ## 3.4.3 (2025-10-31)

     ### Fixed
     - Fixed configuration cache issues.

     ### Dependency updates
     - Updated Gradle to 9.2.0
     EOF
     )"
     ```

4. **Verify release created**
   - Run: `gh release view v<version>`
   - Or run: `gh release view v<version> --web` to open in browser
   - Verify release notes are correct

### 6. Verify publications

1. **Provide verification links to user**
   - Maven Central: https://central.sonatype.com/artifact/io.github.gw-kit/delta-coverage-core/<version>
   - Gradle Plugin Portal: https://plugins.gradle.org/plugin/io.github.gw-kit.delta-coverage
   - GitHub Release: https://github.com/gw-kit/delta-coverage-plugin/releases/tag/v<version>

2. **Inform about sync times**
   - Maven Central: ~30 minutes for full sync
   - Gradle Plugin Portal: ~10-15 minutes for processing
   - GitHub Release: Available immediately

### 7. Post-publication tasks

1. **Prepare for next development cycle** (optional)
   - Ask user: "Would you like to prepare for next development cycle?"
   - If yes:
     - Ask for next version (e.g., 3.4.4-SNAPSHOT)
     - Update gradle.properties with next SNAPSHOT version
     - Create commit: `git commit -am "Prepare for next development iteration"`
     - Push: `git push origin main`

2. **Create CHANGELOG entry for next version** (optional)
   - Add new section at top of CHANGELOG.md:
     ```markdown
     ## <next-version>

     ### New features

     ### Fixed

     ### Dependency updates
     ```

## Success Message

After successful publication, display:

```
🎉 Publication successful!

Version: x.y.z

Published to:
✅ Maven Central: https://central.sonatype.com/artifact/io.github.gw-kit/delta-coverage-core/x.y.z
✅ Gradle Plugin Portal: https://plugins.gradle.org/plugin/io.github.gw-kit.delta-coverage
✅ GitHub Release: https://github.com/gw-kit/delta-coverage-plugin/releases/tag/vx.y.z

Notes:
- Maven Central sync takes ~30 minutes
- Plugin Portal processing takes ~10-15 minutes
- GitHub release is available immediately

Next steps:
1. Verify publications using the links above (wait for sync times)
2. Test installation of new version
3. Announce the release if needed

Would you like to:
- Prepare for next development cycle (bump to x.y.z+1-SNAPSHOT)?
- Test the published artifacts?
```

## Important Notes

- **Order matters**: Publish core to Maven Central first, then gradle plugin to Plugin Portal, then GitHub release
- **No retries**: Both Maven Central and Plugin Portal don't allow republishing same version
- **Signing required**: All Maven Central artifacts must be GPG signed
- **Credentials**: Ensure gradle.properties has:
  - `mavenCentralUsername` and `mavenCentralPassword` for Maven Central
  - `gradle.publish.key` and `gradle.publish.secret` for Plugin Portal
  - GPG signing credentials: `signingInMemoryKey`, `signingInMemoryKeyId`, `signingInMemoryKeyPassword`

## Publishing Tasks Reference

```bash
# Maven Central (delta-coverage-core)
./gradlew :delta-coverage-core:publishAndReleaseToMavenCentral

# Gradle Plugin Portal (delta-coverage-gradle)
./gradlew :delta-coverage-gradle:publishPlugins

# GitHub Release
git tag v<version>
git push origin v<version>
gh release create v<version> --title "Version <version>" --notes "<changelog>"
```

## Troubleshooting

### Maven Central Issues
- **401 Unauthorized**: Check Maven Central credentials in `~/.gradle/gradle.properties`
- **Signing failed**: Verify GPG key credentials
- **Version already exists**: Cannot republish - version is immutable
- **Timeout**: Portal API can be slow - wait and check manually

### Gradle Plugin Portal Issues
- **401 Unauthorized**: Check `gradle.publish.key` and `gradle.publish.secret`
- **Invalid plugin ID**: Verify plugin ID matches registered namespace
- **Version already published**: Cannot republish - version is immutable
- **Functional tests failed**: Fix tests before publishing

### GitHub Release Issues
- **Tag already exists**: Delete tag locally and remotely first:
  ```bash
  git tag -d v<version>
  git push origin :refs/tags/v<version>
  ```
- **Authentication failed**: Run `gh auth login` to re-authenticate
- **Release already exists**: Delete release first:
  ```bash
  gh release delete v<version> --yes
  ```

## Error Handling Rules

- If ANY step fails, STOP immediately and report to user
- Do NOT continue to next step if previous step failed
- If publishing core fails, do NOT publish gradle plugin
- If publishing gradle plugin fails, do NOT create GitHub release
- Always inform user of exact error and suggest fix

## Verification Before Publishing

Before running publish tasks, verify:
- [ ] On `main` branch
- [ ] Branch is up-to-date with origin/main
- [ ] Version in gradle.properties has no SNAPSHOT
- [ ] CHANGELOG.md has section for current version
- [ ] All tests pass
- [ ] Maven Central credentials configured
- [ ] Gradle Plugin Portal credentials configured
- [ ] GPG signing credentials configured