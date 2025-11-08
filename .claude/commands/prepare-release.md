---
description: Prepare a release branch with version updates and changelog
---

# Prepare Release

You are tasked with preparing a release branch for Delta Coverage. This command prepares the project for release but does NOT publish anything.

## Prerequisites

- You should be on a release branch (e.g., `release/3.4.3` or similar)
- Changes should NOT be pushed to main yet
- This prepares the release for review before actual publishing

## Steps to complete:

### 0. Verify branch name (REQUIRED PRECONDITION)

1. **Check current branch**
   - Run: `git branch --show-current`
   - Branch name MUST start with `release/`
   - Examples of valid branch names:
     - `release/3.4.3`
     - `release/3.5.0`
     - `release/4.0.0-beta1`

2. **If branch is NOT a release branch**
   - STOP immediately
   - Display error message:
     ```
     ❌ Error: This command can only be run on a release/ branch.

     Current branch: <branch-name>

     To prepare a release:
     1. Create a release branch: git checkout -b release/x.y.z
     2. Run /prepare-release again
     ```
   - Do NOT proceed with any other steps

3. **If branch is valid**
   - Continue to next step
   - Extract version from branch name if possible (e.g., `release/3.4.3` → `3.4.3`)
   - Use this as suggestion for release version

### 1. Determine the release version

1. **Read current version**
   - Read `gradle.properties` to see current version
   - Current version format: `version=x.y.z` or `version=x.y.z-SNAPSHOT`

2. **Ask user for release version**
   - Ask: "What version are you releasing?"
   - Suggest next semantic version (e.g., if current is 3.4.2, suggest 3.4.3)
   - Version should NOT have SNAPSHOT suffix

### 2. Update project version

1. **Update gradle.properties**
   - Set `version=x.y.z` (the release version, no SNAPSHOT)
   - Use the Edit tool to update the version property

### 3. Verify/Update CHANGELOG.md

1. **Check if changelog has section for this version**
   - Read CHANGELOG.md
   - Look for `## x.y.z` heading matching the release version

2. **If section exists**
   - Verify it has content (not empty)
   - Verify date is present or add it: `## x.y.z (YYYY-MM-DD)` format
   - Ask user if changelog is complete

3. **If section doesn't exist**
   - Ask user to provide changelog content
   - Create new section at the top:
     ```markdown
     ## x.y.z (YYYY-MM-DD)

     ### New features
     - Feature 1

     ### Fixed
     - Fix 1

     ### Dependency updates
     - Updated X to [version](link)
     ```

### 4. Run full test suite

1. **Run tests for both modules**
   - Run: `./gradlew clean build`
   - This runs unit tests and functional tests for both modules
   - If tests fail, STOP and report failures to user

2. **Verify build artifacts**
   - Check that build completed successfully
   - Report any warnings or issues

### 5. Test local publication (optional but recommended)

1. **Test publishing to Maven Local**
   - Run: `./gradlew publishToMavenLocal`
   - This verifies signing and POM generation work correctly
   - Check for any errors in signing process

2. **Verify artifacts**
   - Check: `ls -lh ~/.m2/repository/io/github/gw-kit/delta-coverage-core/<version>/`
   - Verify artifacts exist with signatures (.asc files)

### 6. Create release commit

1. **Stage the changes**
   - Run: `git add gradle.properties CHANGELOG.md`
   - Verify only these files are staged

2. **Create commit**
   - Commit message format: `Prepare release x.y.z`
   - Run: `git commit -m "Prepare release x.y.z"`

3. **Show commit summary**
   - Run: `git show --stat`
   - Display to user what was committed

### 7. Next steps guidance

After completing the above steps, inform the user:

```
✅ Release branch prepared successfully!

Version: x.y.z

Next steps:
1. Review the changes: git show
2. Push to remote: git push origin <branch-name>
3. Create a PR to main branch for review

Files changed:
- gradle.properties: version updated to x.y.z
- CHANGELOG.md: release notes for x.y.z

Would you like me to:
- Push the branch to remote?
- Create a PR to main branch?
```

## Important Notes

- **DO NOT** publish anything to Maven Central or Gradle Plugin Portal
- **DO NOT** create git tags (tags are created during `/publish`)
- **DO NOT** create GitHub releases (done during `/publish`)
- This command only prepares the release for review
- Actual publishing happens after merge to main using `/publish` command

## Verification Checklist

Before finishing, verify:
- [ ] gradle.properties version updated (no SNAPSHOT)
- [ ] CHANGELOG.md has section for this version with content
- [ ] All tests pass (`./gradlew clean build`)
- [ ] Local publication works (`./gradlew publishToMavenLocal`)
- [ ] Changes committed with proper message
- [ ] User informed of next steps

## Error Handling

- If tests fail: STOP and report which tests failed
- If changelog is incomplete: Ask user to provide content
- If version format is invalid: Ask user to correct it
- If unsure about any step: Ask user before proceeding

## Example Workflow

```bash
# 1. Create release branch
git checkout -b release/3.4.3

# 2. Run this command
/prepare-release

# 3. Review changes
git show

# 4. Push and create PR
git push origin release/3.4.3
gh pr create --base main --title "Release 3.4.3" --body "Prepare release 3.4.3"
```