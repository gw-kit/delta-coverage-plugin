# Delta Coverage vs SonarQube

Both tools help improve code quality, but they serve different purposes and have different tradeoffs.

## Quick Comparison

| Aspect | Delta Coverage | SonarQube |
|--------|---------------|-----------|
| **Focus** | Coverage of changed code | Full static analysis + coverage |
| **Setup time** | 5 minutes | Hours to days |
| **Infrastructure** | None (runs locally) | Server required |
| **Cost** | Free (open source) | Free tier limited, paid for advanced |
| **Feedback speed** | Seconds (local) | Minutes (server scan) |
| **PR integration** | GitHub Action available | Native integration |
| **Languages** | JVM (Java, Kotlin, Scala) | 30+ languages |
| **Static analysis** | No | Yes (bugs, vulnerabilities, smells) |

## When to Choose Delta Coverage

Delta Coverage is ideal when you need:

- **Fast local feedback** — Get coverage results in seconds, not minutes
- **No infrastructure** — No servers to maintain or pay for
- **Focused metrics** — Care about coverage of new code, not legacy
- **Gradual improvement** — Enforce standards on new code while improving legacy over time
- **CI simplicity** — Single Gradle task, no external service calls

## When to Choose SonarQube

SonarQube is better when you need:

- **Full static analysis** — Bug detection, security vulnerabilities, code smells
- **Multi-language support** — Projects with Python, JavaScript, Go, etc.
- **Quality gates** — Block merges based on multiple quality metrics
- **Historical trends** — Track quality metrics over months/years
- **Enterprise features** — Portfolio management, governance, compliance

## Using Both Together

You can use both tools. A common pattern:

1. **Delta Coverage** runs on every commit locally and in CI for fast feedback
2. **SonarQube** runs nightly or weekly for comprehensive analysis

This gives developers immediate coverage feedback while maintaining long-term quality tracking.

## Example: Same Change, Different Feedback

Consider adding a new feature with 100 lines of code:

### Delta Coverage

```
./gradlew test deltaCoverage

+----------------------+----------+----------+--------+
| [test] Delta Coverage Stats                         |
+----------------------+----------+----------+--------+
| Class                | Lines    | Branches | Instr. |
+----------------------+----------+----------+--------|
| com.example.Feature  | 92%      | 85%      | 90%    |
+----------------------+----------+----------+--------+

BUILD SUCCESSFUL in 12s
```

**Result:** Pass/fail in 12 seconds. Developer knows exactly what to test next.

### SonarQube

1. Push code to branch
2. Wait for CI to trigger scan
3. Wait for SonarQube to analyze
4. Check PR decoration or dashboard
5. Parse through multiple metrics

**Result:** Feedback in 3-5 minutes. More comprehensive, but slower iteration.

## Migration Path

If you're currently using SonarQube for coverage:

1. Add Delta Coverage for local development first
2. Keep SonarQube for full analysis
3. Optionally, disable SonarQube coverage checks and use Delta Coverage for enforcement

See [CI Integration](../guides/ci-integration.md) for setup examples.
