# Delta Coverage

[![GitHub Release](https://img.shields.io/github/v/release/SurpSG/delta-coverage-plugin)](https://github.com/gw-kit/delta-coverage-plugin/releases)
[![Build](https://github.com/gw-kit/delta-coverage-plugin/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/gw-kit/delta-coverage-plugin/actions/workflows/build.yml)
[![GitHub issues](https://img.shields.io/github/issues/SurpSG/delta-coverage-plugin)](https://github.com/SurpSG/delta-coverage-plugin/issues)
[![GitHub stars](https://img.shields.io/github/stars/SurpSG/delta-coverage-plugin?style=flat-square)](https://github.com/SurpSG/delta-coverage-plugin/stargazers)

![aggregated](https://raw.githubusercontent.com/gw-kit/coverage-badges/refs/heads/main/delta-coverage-plugin/badges/aggregated.svg)
![functionalTest](https://raw.githubusercontent.com/gw-kit/coverage-badges/refs/heads/main/delta-coverage-plugin/badges/functionalTest.svg)
![test](https://raw.githubusercontent.com/gw-kit/coverage-badges/refs/heads/main/delta-coverage-plugin/badges/test.svg)

**Code coverage for what matters — your changes.**

Delta Coverage is a Gradle plugin that computes code coverage of new/modified code based on a git diff. Focus on testing what you've changed, not the entire codebase.

📖 **[Full Documentation](https://gw-kit.github.io/delta-coverage-plugin)**

<img src="https://user-images.githubusercontent.com/8483470/77781538-a74f3480-704d-11ea-9e39-051f1001b88a.png" width="600" alt="Delta Coverage HTML Report"/>

## Quick Start

**1. Apply the plugin** (root project)

```kotlin
plugins {
    id("io.github.gw-kit.delta-coverage") version "3.6.0"
}
```

**2. Configure diff source**

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith.set("refs/remotes/origin/main")
}
```

**3. Run**

```bash
./gradlew test deltaCoverage
```

Reports are generated in `build/reports/coverage-reports/`.

## Compatibility

| Delta Coverage | Gradle | Min JVM |
|----------------|--------|---------|
| 3.x | 7.6.4+ | 17 |
| 2.5.x | 6.7.1 - 8.10.2 | 11 |

## Related Projects

- [delta-coverage-action](https://github.com/gw-kit/delta-coverage-action) — GitHub Action for PR comments
- [delta-coverage-cli](delta-coverage-cli/README.md) — Standalone CLI tool
- [cover-jet-plugin](https://github.com/gw-kit/cover-jet-plugin) — IntelliJ coverage for Gradle

## License

MIT
