# Content Plan: Delta Coverage Plugin

**Date:** 2025-02-07
**Author:** Sergii
**Status:** Draft

## Priority Content Assets

### 1. Hero GIF (MANDATORY, High Impact)

10-second terminal recording showing:

```
edit file → run test → deltaCoverage → instant result
```

**Expected output:**
```
./gradlew test deltaCoverage

> Task :deltaCoverage

Delta Coverage Report
─────────────────────
Changed lines: 14
Covered: 12
Delta coverage: 85% ✓

BUILD SUCCESSFUL
```

**Placement:** Top of README and landing page.

**Tools:** asciinema, Terminalizer, or screen recording + GIF conversion.

**Visual proof beats 5 pages of docs.**

---

### 2. Demo Repository

Create: `gw-kit/delta-coverage-demo-kotlin`

**Structure:**
- One class (Calculator.kt)
- One test (CalculatorTest.kt)
- Minimal build.gradle.kts with plugin configured
- README with step-by-step instructions

**Purpose:** Copy-paste adoption fuel.

**Flow to demonstrate:**
1. Clone repo
2. Make a change to Calculator.kt
3. Run `./gradlew test deltaCoverage`
4. See coverage result

---

### 3. Killer Blog Post

**Title options:**
- "Local Delta Coverage in Gradle: Stop Waiting for CI"
- "How we reduced CI time by 40% using delta coverage"
- "Coverage feedback in seconds, not minutes"

**Structure:**
1. **Problem:** CI slow → Coverage gates slow → Devs guess locally
2. **Solution:** Introduce delta coverage concept
3. **Demo:** Show terminal output, before/after timings
4. **Setup:** Quick config snippet
5. **CTA:** Link to plugin, docs, demo repo

**Distribution:**
- dev.to
- Medium
- Reddit: r/java, r/kotlin, r/gradle
- Kotlin Slack
- Hacker News (Show HN)

---

### 4. PR Comment Screenshot

Showcase delta-coverage-action PR decoration.

**Example comment format:**
```markdown
## Delta Coverage Report

| Metric | Value |
|--------|-------|
| Changed lines | 14 |
| Covered | 12 |
| Coverage | 85% ✓ |

### Uncovered lines
- `Foo.kt:42`
- `Bar.kt:15-18`
```

**Action:** Create polished screenshot for docs and README.

---

## Documentation Pages (Priority Order)

### Must-Write Well

1. **guides/local-development.md**
   - THE key workflow page
   - Show: branch → code → subset tests → deltaCoverage → seconds
   - Include timing comparison vs SonarQube workflow
   - Terminal output examples

2. **configuration/report-views.md** + **guides/testing-pyramid.md**
   - Unique feature, no competitor offers this
   - Example: project with test, integrationTest, e2eTest
   - Show coverage per test type + aggregated
   - Why it matters: 95% unit coverage but 0% integration for same code

3. **comparison/vs-sonarqube.md**
   - Comparison table + narrative
   - SEO target: "sonar alternative gradle"
   - Be fair: when SonarQube is still right

---

## Content Calendar

| Week | Deliverable |
|------|-------------|
| 1 | Hero GIF, landing page, quick-start |
| 2 | Demo repo, local-development guide |
| 3 | vs-sonarqube page, PR screenshot |
| 4 | Blog post draft |
| 5 | Blog post publish + social distribution |

---

## Open Questions

1. Who records the GIF? (screen recording setup needed)
2. Where to host demo repo? (gw-kit org or separate?)
3. Which platform for blog post first? (dev.to vs Medium)
4. Need PR comment redesign before screenshot?