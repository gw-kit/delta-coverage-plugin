# Contributing to Delta Coverage Plugin

Thank you for your interest in contributing to Delta Coverage Plugin! This guide covers the project structure, branching model, and release process.

## Project Structure

Delta Coverage Plugin is a monorepo containing several related code coverage tools:

| Module | Description | Artifact |
|--------|-------------|----------|
| `core` | Shared coverage model, diff engine, reporting | `coverage-toolkit-core` |
| `delta-coverage-gradle` | Gradle plugin for delta coverage analysis | `delta-coverage-gradle-plugin` |
| `offlins-gradle` | Gradle plugin for JaCoCo offline instrumentation | `offlins-gradle-plugin` |
| `cover-jet-gradle` | Gradle plugin for IntelliJ coverage engine | `cover-jet-gradle-plugin` |
| `sampling` | Test-to-code mapping via JFR stack sampling | `coverage-sampling` |
| `sampling-gradle` | Gradle integration for sampling | `sampling-gradle-plugin` |
| `build-logic` | Shared build conventions and publishing config | — |

Each module is versioned and released independently.

### Related Projects

These projects live in separate repositories:

- [gradle-probe](https://github.com/gw-kit/gradle-probe) — Testing library for Gradle plugins
- Maven plugin (planned)
- IntelliJ plugin (planned)

## Branching Model

The project follows a trunk-based development model.

### Branches

- **`main`** — The default branch. Always in a buildable, testable state. Snapshot artifacts are published from every push.
- **Feature branches** — Short-lived branches for developing new features or fixes. Merged to `main` via pull request.
- **Release branches** — Per-module branches for preparing a release. Named `release/<module>/<version>` (e.g., `release/delta-coverage/3.2.0`).

### Workflow

```
main ●──●──●──●──────●──●──●──●
          \        ↗
feature    ●──●──●
```

1. Create a feature branch from `main`.
2. Develop, commit, push.
3. Open a pull request to `main`.
4. After review, merge to `main`.

## Making Changes

### Setting Up

```bash
git clone https://github.com/gw-kit/delta-coverage-plugin.git
cd delta-coverage-plugin
./gradlew build
```

### Guidelines

- Keep pull requests focused on a single module or concern when possible.
- Ensure `./gradlew build` passes before pushing.
- Add tests for new functionality.
- Update documentation if behavior changes.

## Release Process

Each module is released independently. A module's release does not affect other modules.

### Versioning

Each module maintains its own version. Tags are prefixed by module name:

```
delta-coverage/3.2.0
offlins/1.1.0
cover-jet/1.0.3
sampling/0.1.0
```

### Snapshots

Snapshot artifacts are published automatically on every push to `main`. Only modules with changes since the last snapshot are published.

### Releasing a Module

#### 1. Create a release branch

```bash
git checkout main
git pull
git checkout -b release/delta-coverage/3.2.0
```

#### 2. Prepare the release

On the release branch, make release-specific changes:

- Set the release version (remove `-SNAPSHOT` suffix)
- Update `CHANGELOG.md`
- Update documentation if needed

Push commits to the release branch. Each push triggers an auto-merge to `main`, keeping it up to date with release changes.

```
main ●──●──●──●───────────●(auto)──●(auto)──●──●
               \         ↗        ↗
release         ●───────●────────●
                changelog  docs    ← tag here
```

#### 3. Tag the release

When the release branch is ready, tag the **last commit on the release branch**:

```bash
git tag delta-coverage/3.2.0
git push origin delta-coverage/3.2.0
```

CI detects the tag push, builds the artifact from that exact commit, and publishes it.

> **Why tag the release branch, not main?**
> The tag points to precisely what was published. Main may contain newer commits from other modules that shouldn't be part of this release. The tagged commit is immutable and verifiable.

#### 4. Clean up

After the tag is pushed and CI has published successfully, delete the release branch:

```bash
git push origin --delete release/delta-coverage/3.2.0
```

### Release Checklist

- [ ] Release branch created from `main`
- [ ] Version updated
- [ ] Changelog updated
- [ ] Documentation updated
- [ ] All commits auto-merged to `main`
- [ ] Release branch tagged
- [ ] CI published successfully
- [ ] Release branch deleted
- [ ] GitHub Release created (optional)

## CI/CD

| Trigger | Action |
|---------|--------|
| Push to `main` | Build, test all modules. Publish snapshots for changed modules. |
| Pull request | Build, test affected modules. |
| Push to `release/*` | Build, test. Auto-merge to `main`. |
| Tag push (`<module>/<version>`) | Build, test, publish release artifact. |

### Auto-Merge Conflicts

If an auto-merge from a release branch to `main` fails due to conflicts, CI will fail. Resolve by rebasing the release branch on `main` and pushing again.

## Questions?

Open an issue or start a discussion in the repository. We're happy to help!
