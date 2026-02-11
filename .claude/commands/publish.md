---
description: Trigger the 🚀 Release workflow to publish and create a GitHub Release
---

# Publish Delta Coverage

You are tasked with triggering the 🚀 Release workflow to publish a Delta Coverage module and create a GitHub Release.

This command should be run AFTER:
- Release branch has been prepared (using `/prepare-release`)
- You are on a release branch (`release/<module>/<version>`)

## Prerequisites Verification

Before starting, verify:

1. **Check current branch**
   - Run: `git branch --show-current`
   - Must be on a `release/<module>/<version>` branch (e.g., `release/delta-coverage-gradle/3.2.0`)
   - If not on a release branch, STOP and inform user

2. **Parse branch name**
   - Extract module and version from branch name
   - Branch format: `release/<module>/<version>`
   - Example: `release/delta-coverage-gradle/3.2.0` → module=`delta-coverage-gradle`, version=`3.2.0`

3. **Verify branch is pushed to remote**
   - Run: `git fetch origin && git status`
   - Must be up-to-date with remote tracking branch
   - If not pushed, push to remote first: `git push -u origin <branch-name>`

4. **Read version from gradle.properties**
   - Read `gradle.properties` to get version
   - Version should NOT contain SNAPSHOT
   - If SNAPSHOT found, STOP and inform user

5. **Verify changelog exists**
   - Read CHANGELOG.md
   - Must have section for current version
   - If not found, STOP and inform user

## Steps to complete:

### 1. Trigger the 🚀 Release workflow

1. **Dispatch the workflow**
   - Run: `gh workflow run release.yml --ref <branch-name> -f module=<module>`
   - Example:
     ```bash
     gh workflow run release.yml --ref release/delta-coverage-gradle/3.2.0 -f module=delta-coverage-gradle
     ```

2. **Confirm workflow was triggered**
   - If the command succeeds, the workflow has been queued

### 2. Monitor workflow execution

1. **Get the workflow run ID**
   - Wait a few seconds for the run to appear
   - Run: `sleep 5 && gh run list --workflow=release.yml --limit=1 --json databaseId,status,headBranch --jq '.[0]'`
   - Verify it matches the current branch

2. **Watch the workflow**
   - Run: `gh run watch <run-id>`
   - This streams live status updates until the workflow completes
   - If the workflow fails, STOP and report the failure

### 3. Verify the release

1. **Check the GitHub Release was created**
   - Run: `gh release view <module>/<version>`
   - Example: `gh release view delta-coverage-gradle/3.2.0`
   - If the release exists, publishing was successful

2. **Provide verification links to user**
   - GitHub Release: `https://github.com/gw-kit/delta-coverage-plugin/releases/tag/<module>/<version>`
   - For `delta-coverage-gradle`: Gradle Plugin Portal at `https://plugins.gradle.org/plugin/io.github.gw-kit.delta-coverage`
   - For other modules: Maven Central at `https://central.sonatype.com/artifact/io.github.gw-kit/<module>/<version>`

3. **Inform about sync times**
   - Maven Central: ~30 minutes for full sync
   - Gradle Plugin Portal: ~10-15 minutes for processing
   - GitHub Release: Available immediately

### 4. Clean up

1. **Delete the release branch**
   - Ask user if they want to delete the release branch
   - Run: `git push origin --delete <branch-name>`
   - Run: `git checkout main && git pull`

## Success Message

After successful publication, display:

```
🎉 Release triggered successfully!

Module: <module>
Version: <version>
Tag: <module>/<version>

The 🚀 Release workflow has:
✅ Built and tested the project
✅ Published the artifact
✅ Created tag: <module>/<version>
✅ Created GitHub Release

Verification:
- GitHub Release: https://github.com/gw-kit/delta-coverage-plugin/releases/tag/<module>/<version>

Notes:
- Maven Central sync takes ~30 minutes
- Plugin Portal processing takes ~10-15 minutes
- GitHub release is available immediately
```

## Important Notes

- **No local credentials needed**: Publishing is handled entirely by the CI workflow using repository secrets
- **No local Gradle publish commands**: Everything runs in CI
- **No manual tag creation**: The workflow creates and pushes the tag automatically
- **No manual GitHub Release**: The workflow creates it automatically with generated notes
- **Branch validation**: The workflow validates that the branch name matches the selected module
- **Idempotent tags**: If the tag already exists, the workflow will fail — do not re-run for the same version

## Error Handling Rules

- If the workflow fails, check the logs: `gh run view <run-id> --log-failed`
- If branch validation fails, verify you are on the correct `release/<module>/<version>` branch
- If publishing fails, check that repository secrets are configured
- Do NOT attempt to publish manually — all publishing goes through the workflow

## Verification Before Publishing

Before triggering the workflow, verify:
- [ ] On a `release/<module>/<version>` branch
- [ ] Branch is pushed to remote
- [ ] Version in gradle.properties has no SNAPSHOT
- [ ] CHANGELOG.md has section for current version
- [ ] All commits have been auto-merged to `main`