# Marketing Strategy: Delta Coverage Plugin

**Date:** 2025-02-07
**Author:** Sergii
**Status:** Draft

## Current State

The plugin is technically mature (844+ commits, 27+ releases), but discoverability and adoption are behind code quality.

**Problems:**
- GitHub repo has no description, website, or topics
- README is ~500 lines — comprehensive but overwhelming
- Killer features are buried
- Sells features, not outcomes

## Messaging & Positioning

### Core Message

**Wrong:** "Delta coverage plugin with views, engines, and formats."

**Right:** "Fast local feedback on coverage for only what you changed."

### Emotional Hook

> Write code → run one test → instantly know if your change is covered.

Sonar can't do this. Codecov can't do this. Most teams don't realize this is possible.

### Elevator Pitch

> Delta Coverage brings test coverage to developer workflow: run it locally, check only changed code, and get feedback in seconds — no server required.

### Marketing Angles

| Technical Feature | Marketing Message |
|-------------------|-------------------|
| Local-first delta coverage | "Shift coverage left" / "Coverage at dev time" |
| No server required | "Zero infrastructure coverage gates" |
| Test pyramid views | "Separate unit / integration / e2e coverage automatically" |
| Fast feedback | "Stop discovering uncovered code in CI" |

---

## Branding

"delta-coverage-plugin" is correct but forgettable.

**Consideration:** Adopt "DeltaCov" as short brand name.
- Memorable, searchable
- Keep artifact coordinates as-is
- Use brand in docs, social, talks

---

## Competitive Positioning

| Feature | Delta Coverage | SonarQube | Codecov |
|---------|---------------|-----------|---------|
| Runs locally | Instant | Needs server | Needs CI |
| Full test suite required | No | Yes | Yes |
| Test pyramid views | Built-in | N/A | N/A |
| Free | Always | Community Edition | Limited |
| Gradle-native | First-class | External | External |
| Setup time | ~2 min | Hours | ~30 min |
| Feedback speed | Seconds | Minutes | Minutes |
| CI infrastructure | None | Required | Required |

**When SonarQube is still right:** Enterprise compliance, multi-language monorepos, extensive rule sets.

---

## GitHub Repository Improvements

### Description
"Gradle plugin for delta code coverage — check coverage of only changed code. Faster than SonarQube, runs locally, free forever."

### Topics
`gradle-plugin`, `code-coverage`, `jacoco`, `kotlin`, `java`, `testing`, `ci-cd`, `pull-request`, `developer-tools`, `sonarqube-alternative`

### Website URL
Set to docs site URL once live.

---

## Social Proof

- "Used in production by teams with 60+ backend developers"
- "Tens of thousands of tests at scale"
- Consider "Used by" section (if company logos approved)

---

## Growth Channels

### Communities
- Reddit: r/java, r/kotlin, r/gradle
- Kotlin Slack: #gradle, #testing
- Gradle community forums
- Hacker News (Show HN)
- Twitter/X, Mastodon — tag @gradle on releases

### Listings
- Submit to awesome-gradle, awesome-kotlin
- Enrich Gradle Plugin Portal description
- Guest post on Gradle blog or Kotlin blog

### Releases
Every release = small marketing event:
- Not just "v3.5.1 released"
- But "Delta Coverage 3.5.1: IntelliJ engine now supports exclude filters, plus 40% faster report generation"

---

## Open Questions

1. Adopt "DeltaCov" branding?
2. Custom domain (deltacov.dev)?
3. Ask companies for logo permission?
4. Conference talk proposals (KotlinConf, Devoxx)?