# Changelog

## Unreleased

### Changes

- Minimum supported Gradle version raised to 8.14 (dropped support for Gradle 7.x).
- Minimum supported JVM version raised to 21 (dropped support for JVM 17).

### Dependency updates

- Updated Gradle to [9.7.1](https://github.com/gradle/gradle/releases/tag/v9.7.1).

---

## 3.6.0

### New Features

- **CLI Tool** — Standalone command-line interface for running delta coverage without the Gradle plugin.
  Supports both JaCoCo and IntelliJ engines, glob patterns, YAML config, and all report formats.
- **Explain Report** — Debug and troubleshoot Delta Coverage configuration.
- **Per-view class filters** — `includeClasses` and `excludeClasses` at the view level, in addition to global `excludeClasses`.

### Deprecated

- `matchClasses` property in `ReportView` — use `includeClasses` instead.

### Fixed

- Fixed eager task evaluation during plugin configuration phase (improved configuration cache compatibility).

---

## 3.5.0

### New Features

- Exclude filters for IntelliJ full coverage reports.
- Custom class filters via `matchClasses` in report views.
- Maven Central publishing support.

### Fixed

- Configuration cache issues.
- Full coverage custom view handling with IntelliJ engine.
- `excludeClasses` now correctly applies to full coverage reports (IntelliJ engine).

---

## Older Versions

See the full changelog in [CHANGELOG.md](https://github.com/gw-kit/delta-coverage-plugin/blob/main/CHANGELOG.md) or [GitHub Releases](https://github.com/gw-kit/delta-coverage-plugin/releases).

For migration between major versions, see the [Migration Guide](getting-started/migration.md).
