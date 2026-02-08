# ADR: Test-to-Code Mapping via Stack Sampling

**Status:** Proposed  
**Date:** 2025-02-01  
**Author:** Sergii  
**Project:** delta-coverage-gradle-plugin

## Context and Problem Statement

Developers need to understand which tests cover which parts of their codebase, especially when making changes. Current coverage tools (JaCoCo) provide aggregate coverage data but don't answer:

1. "Which specific tests exercise this changed method?"
2. "What tests should I run to verify my changes?"
3. "Are there gaps in test specificity for modified code?"

This information is crucial for:
- Efficient local development (run only relevant tests)
- PR validation (fast feedback on changed code)
- Understanding test quality beyond simple coverage percentages

## Decision Drivers

- **Minimal performance overhead** — must not significantly slow down test execution
- **No bytecode modification** — avoid complexity and conflicts with other tools
- **Call depth visibility** — understand direct vs transitive test coverage
- **Integration with existing delta-coverage** — leverage current diff analysis
- **Incremental delivery** — gather user feedback at each stage

## Considered Options

### Option 1: Per-Test JaCoCo Exec Files

Dump JaCoCo coverage after each test using JMX MBean.

**Pros:**
- Uses existing JaCoCo infrastructure
- Precise line coverage

**Cons:**
- High overhead (dump per test)
- Many small files to process
- No call depth information
- Requires JaCoCo agent

### Option 2: Custom Instrumentation Agent

Build custom agent that tags probe hits with test context.

**Pros:**
- Fine-grained control
- Single-pass collection

**Cons:**
- Complex implementation
- Bytecode modification required
- ThreadLocal management for parallel tests
- Maintenance burden

### Option 3: Stack Sampling (Selected)

Sample JVM stack traces periodically during test execution, similar to async-profiler approach.

**Pros:**
- Minimal, predictable overhead (~1%)
- No bytecode modification
- Natural call depth information
- Lightweight implementation
- Works with any test framework

**Cons:**
- Statistical, not exact (may miss very short methods)
- Requires correlation of samples to test boundaries

## Decision Outcome

**Chosen option: Stack Sampling**

For test-to-code *mapping*, we need relationships, not exact hit counts. The sampling approach provides:

1. Call stack depth naturally
2. Predictable low overhead
3. Simple implementation path
4. No conflicts with existing tooling

The trade-off (statistical vs exact) is acceptable for the mapping use case.

### Derived Metrics from Sampling

Beyond test-to-code mapping, the sampling data enables additional insights:

| Metric | Description | Value |
|--------|-------------|-------|
| **Hit Counts** | How many times a method appears in samples | Identifies hot paths, heavily-used code |
| **Test Focus** | Hit distribution per test | Reveals if test is focused or sprawling |
| **Change Risk** | Hits × test count for changed code | Prioritize review for high-impact changes |
| **Call Patterns** | Hit ratios between caller/callee | Detect loops, retries, unexpected behavior |

**Hit Count Use Cases:**

1. **Hot Path Identification** — Methods with high hits are performance-critical
2. **Test Quality Signal** — Single hit = focused test; thousands of hits = potentially too broad
3. **Change Impact Assessment** — High-hit changed method = higher risk, more careful review
4. **Anomaly Detection** — Unexpected hit patterns may indicate bugs or inefficiencies

## Technical Design

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Test JVM                                                   │
│  ┌───────────────────┐     ┌───────────────────────────┐   │
│  │ JUnit Platform    │     │ StackSampler              │   │
│  │ TestListener      │────►│ - Periodic sampling       │   │
│  │ (test boundaries) │     │ - Thread filtering        │   │
│  └───────────────────┘     │ - Sample storage          │   │
│                            └───────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
                          ┌───────────────────────────┐
                          │ test-mapping.json         │
                          │ method → [tests + depth]  │
                          └───────────────────────────┘
                                        │
                    ┌───────────────────┴───────────────────┐
                    ▼                                       ▼
          ┌─────────────────┐                   ┌─────────────────┐
          │ Mapping Report  │                   │ Test Selection  │
          │ (coverage gaps) │                   │ (TIA)           │
          └─────────────────┘                   └─────────────────┘
```

### Core Components

#### 1. StackSampler

```kotlin
class StackSampler(
    private val intervalMs: Long = 1,
    private val maxDepth: Int = 50,
    private val excludePackages: Set<String> = defaultExcludes
) {
    private val samples = ConcurrentLinkedQueue<Sample>()
    private val currentTest = ThreadLocal<TestIdentifier?>()
    
    data class Sample(
        val testId: TestIdentifier,
        val timestamp: Long,
        val frames: List<StackFrame>
    )
    
    data class StackFrame(
        val className: String,
        val methodName: String,
        val lineNumber: Int
    )
}
```

#### 2. TestListener Integration

```kotlin
class DeltaCoverageTestListener : TestExecutionListener {
    
    override fun executionStarted(testIdentifier: TestIdentifier) {
        if (testIdentifier.isTest) {
            sampler.setCurrentTest(testIdentifier)
        }
    }
    
    override fun executionFinished(testIdentifier: TestIdentifier, result: TestExecutionResult) {
        if (testIdentifier.isTest) {
            sampler.clearCurrentTest()
        }
    }
}
```

#### 3. Mapping File Format

```json
{
  "version": 1,
  "generatedAt": "2025-02-01T10:30:00Z",
  "commitSha": "abc123def",
  "samplingIntervalMs": 1,
  "summary": {
    "totalTests": 847,
    "totalMethods": 1234,
    "totalSamples": 98234
  },
  "mappings": {
    "com.example.Calculator": {
      "multiply(int,int)": {
        "startLine": 45,
        "endLine": 52,
        "visibility": "public",
        "totalHits": 365,
        "tests": [
          {
            "id": "com.example.CalculatorTest#shouldMultiply",
            "depth": 1,
            "samples": 342,
            "hits": 342
          },
          {
            "id": "com.example.OrderServiceTest#shouldCalculateTotal",
            "depth": 3,
            "samples": 23,
            "hits": 23
          }
        ]
      },
      "validate(int)": {
        "startLine": 60,
        "endLine": 65,
        "visibility": "private",
        "totalHits": 0,
        "tests": []
      }
    }
  },
  "hotMethods": [
    {
      "method": "com.example.Validator#validate(Object)",
      "totalHits": 4521,
      "testCount": 23
    },
    {
      "method": "com.example.Calculator#add(int,int)",
      "totalHits": 890,
      "testCount": 15
    }
  ]
}
```

**Note on Samples vs Hits:**
- `samples` — number of times method appeared in stack samples (statistical)
- `hits` — estimated call count derived from samples (may equal samples, or be computed differently)
- `totalHits` — aggregate across all tests

### Configuration DSL

```kotlin
deltaCoverage {
    testMapping {
        enabled = true
        
        sampling {
            intervalMs = 1                    // Sampling frequency
            maxDepth = 50                     // Max stack depth to capture
        }
        
        analysis {
            maxReportedDepth = 3              // Filter deep transitive calls
            privateMethodHandling = INCLUDE   // INCLUDE | EXCLUDE | COLLAPSE_TO_PUBLIC
            excludePackages = listOf(         // Noise reduction
                "org.junit",
                "org.gradle",
                "java.lang.reflect"
            )
        }
        
        output {
            mappingFile = file("test-mapping.json")
            reportDir = file("build/reports/delta-coverage/test-mapping")
        }
    }
}
```

---

## Implementation Phases

### Phase 1: Core Sampling Infrastructure

**Goal:** Prove the sampling approach works and collects useful data.

**Scope:**
- [ ] Implement `StackSampler` with configurable interval
- [ ] Implement basic `TestExecutionListener` for JUnit Platform
- [ ] Output raw samples to JSON file
- [ ] Basic Gradle plugin extension for configuration
- [ ] Manual verification with sample project

**Deliverables:**
- `delta-coverage-sampling` module (or internal package)
- Raw sample output: `test-samples.json`
- Documentation: "How to enable sampling"

**User Feedback Questions:**
- Does sampling noticeably slow down tests?
- Are the captured stacks useful/readable?
- What's missing from the raw output?

**Success Criteria:**
- Overhead < 5% on test suite
- All test methods detected in samples
- Stack traces include expected application code

**Estimated Effort:** 1-2 weeks

---

### Phase 2: Test-to-Method Mapping

**Goal:** Transform raw samples into actionable method-level mapping.

**Scope:**
- [ ] Build mapping aggregator (samples → method → tests)
- [ ] Implement depth calculation from stack frames
- [ ] Handle method signature extraction
- [ ] Implement visibility detection (public/private)
- [ ] Generate `test-mapping.json`
- [ ] Add package exclusion filters

**Deliverables:**
- `test-mapping.json` generation
- Mapping statistics (tests per method, depth distribution)

**User Feedback Questions:**
- Is method granularity right, or need line-level?
- Are the depth numbers intuitive?
- What filtering options are needed?

**Success Criteria:**
- Correct test→method associations verified manually
- Depth values match actual call chains
- Mapping file < 10MB for typical projects

**Estimated Effort:** 1-2 weeks

---

### Phase 3: Integration with Git Diff

**Goal:** Connect mapping to changed code for delta analysis.

**Scope:**
- [ ] Integrate with existing delta-coverage diff analysis
- [ ] Filter mapping to only changed methods/lines
- [ ] Detect unmapped changes (new code without tests)
- [ ] Report: "Changed code coverage by test"

**Deliverables:**
- Console output: "3 tests cover your changes"
- Report showing changed methods and their tests
- Warning for changed code with no test coverage

**User Feedback Questions:**
- Is the diff detection accurate?
- What additional context needed in reports?
- How should new (unmapped) code be handled?

**Success Criteria:**
- Changed methods correctly identified
- Test mapping correlates with actual coverage
- Clear identification of coverage gaps

**Estimated Effort:** 1-2 weeks

---

### Phase 4: Test Impact Analysis (TIA)

**Goal:** Enable running only tests affected by changes.

**Scope:**
- [ ] Implement `testImpact` task (informational)
- [ ] Implement `testAffected` task (runs filtered tests)
- [ ] Change detection: git diff, explicit files, Gradle inputs
- [ ] Depth-based filtering for test selection
- [ ] Stale mapping detection and warnings

**Deliverables:**
- `./gradlew testImpact` — shows affected tests
- `./gradlew testAffected` — runs only affected tests
- `./gradlew testAffected --max-depth=2`

**User Feedback Questions:**
- Is test selection accurate (no false negatives)?
- How much time saved vs full test run?
- What's the ideal mapping refresh strategy?

**Success Criteria:**
- No missed tests (false negatives) for changed code
- 50%+ reduction in test execution time for typical changes
- Clear warnings when mapping may be stale

**Estimated Effort:** 2-3 weeks

---

### Phase 5: HTML Report Generation

**Goal:** Visual report for test-to-code mapping analysis.

**Scope:**
- [ ] HTML report template
- [ ] Source code view with test annotations
- [ ] Depth visualization
- [ ] Coverage gap highlighting
- [ ] Integration with existing delta-coverage reports

**Deliverables:**
- HTML report: `build/reports/delta-coverage/test-mapping/index.html`
- Per-file views with line annotations
- Summary dashboard

**Example Report Layout:**
```
┌─────────────────────────────────────────────────────────────┐
│ Test Mapping Report                          Generated: ... │
├─────────────────────────────────────────────────────────────┤
│ Summary:                                                    │
│   Changed files: 5                                          │
│   Changed methods: 12                                       │
│   Covered by tests: 10 (83%)                                │
│   Coverage gaps: 2 methods                                  │
├─────────────────────────────────────────────────────────────┤
│ Hot Methods (by hit count)                    [Toggle View] │
│ ─────────────────────────────────────────────────────────── │
│   1. Validator.validate()      4521 hits   23 tests   ⚠️    │
│   2. StringUtils.trim()        3200 hits   45 tests        │
│   3. Calculator.add()           890 hits   15 tests        │
├─────────────────────────────────────────────────────────────┤
│ Calculator.java                                             │
│ ─────────────────────────────────────────────────────────── │
│ 45 │ + public int multiply(int a, int b) {    365 hits     │
│    │     ├─ CalculatorTest.shouldMultiply (d:1, h:342)     │
│    │     └─ OrderServiceTest.calcTotal (d:3, h:23)         │
│ 46 │ +     return a * b;                                    │
│    │                                                        │
│ 60 │ + private int validate(int x) {          ⚠️ NO TESTS   │
│ 61 │ +     return x > 0 ? x : 0;              0 hits        │
│ 62 │ + }                                                    │
└─────────────────────────────────────────────────────────────┘
```

Legend: d = depth, h = hits

**User Feedback Questions:**
- Is the report actionable?
- What additional views needed?
- Integration with CI reporting?

**Success Criteria:**
- Report clearly shows coverage gaps
- Navigation between files works smoothly
- Renders correctly in major browsers

**Estimated Effort:** 2-3 weeks

---

### Phase 6: Advanced Features (Future)

**Potential enhancements based on user feedback:**

- [ ] **IDE Integration** — IntelliJ plugin reading mapping file
- [ ] **Watch Mode** — Continuous test selection during development
- [ ] **Mapping Diff** — Compare mappings between commits
- [ ] **JFR Integration** — Alternative to custom sampler
- [ ] **Async-Profiler Integration** — Native sampling for lower overhead
- [ ] **Multi-Module Support** — Aggregate mappings across modules
- [ ] **Test Prioritization** — Run most relevant tests first

---

## Configuration Reference

### Full Configuration Example

```kotlin
// build.gradle.kts
plugins {
    id("io.github.gw-kit.delta-coverage") version "X.Y.Z"
}

deltaCoverage {
    // Existing delta coverage config...
    
    testMapping {
        // Enable/disable feature
        enabled = true
        
        // Sampling configuration
        sampling {
            intervalMs = 1                      // Default: 1ms
            maxDepth = 50                       // Default: 50 frames
            includeLineNumbers = true           // Default: true
        }
        
        // Analysis options
        analysis {
            maxReportedDepth = 5                // Default: unlimited
            privateMethodHandling = INCLUDE     // INCLUDE | EXCLUDE | COLLAPSE_TO_PUBLIC
            
            // Hit count analysis
            hitCounts {
                enabled = true                  // Track method hit counts
                hotMethodThreshold = 100        // Flag methods with hits > threshold
                topHotMethodsCount = 20         // Include top N hot methods in summary
            }
            
            excludePackages = listOf(
                "org.junit",
                "org.gradle", 
                "jdk.internal",
                "sun.reflect"
            )
            
            excludeClasses = listOf(
                ".*\\$\\$.*",                   // Proxy classes
                ".*_Generated"                  // Generated code
            )
        }
        
        // Output configuration  
        output {
            mappingFile = file("test-mapping.json")
            htmlReportEnabled = true
            htmlReportDir = file("build/reports/delta-coverage/test-mapping")
        }
        
        // Test impact analysis
        impact {
            enabled = true
            
            changeDetection {
                // Git-based (default)
                git {
                    compareWith = "origin/main"
                }
                
                // Or explicit files
                // files = listOf("src/main/java/com/example/Changed.java")
                
                // Or Gradle incremental
                // useGradleInputChanges = true
            }
            
            staleMappingWarningDays = 7         // Warn if mapping older than N days
        }
    }
}
```

### Gradle Tasks

| Task | Description |
|------|-------------|
| `test` | With `testMapping.enabled=true`, collects samples during execution |
| `generateTestMapping` | Generates `test-mapping.json` from collected samples |
| `testMappingReport` | Generates HTML report |
| `testImpact` | Shows which tests are affected by current changes |
| `testAffected` | Runs only tests affected by current changes |
| `hotMethods` | Shows most frequently hit methods across all tests |

### Command Line Options

```bash
# Collect mapping during test run
./gradlew test -Pdelta.testMapping.enabled=true

# Generate report only
./gradlew testMappingReport

# Show affected tests for current changes
./gradlew testImpact

# Show affected tests for specific files
./gradlew testImpact --changed=src/main/java/com/example/Calculator.java

# Show affected tests since specific commit
./gradlew testImpact --since=HEAD~3

# Run only affected tests
./gradlew testAffected

# Run affected tests with depth filter
./gradlew testAffected --max-depth=2

# Force fresh mapping collection
./gradlew testAffected --refresh-mapping

# Show hot methods (most frequently called)
./gradlew hotMethods

# Show hot methods for specific test
./gradlew hotMethods --test="OrderServiceTest.shouldProcessOrder"

# Show hot methods with custom threshold
./gradlew hotMethods --min-hits=500

# Show hot methods in changed files only
./gradlew hotMethods --changed-only
```

---

## Risks and Mitigations

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Sampling misses short methods | Medium | Medium | Lower interval, document limitation |
| High memory for large test suites | Medium | Low | Streaming write, configurable buffer |
| Parallel test interference | High | Medium | ThreadLocal isolation, careful thread detection |
| Stale mapping causes missed tests | High | Medium | Warnings, freshness checks, CI integration |
| Complex multi-module setups | Medium | Medium | Phase 6 scope, start with single module |

---

## Success Metrics

### Phase 1-2 (Core)
- Overhead < 5% on test execution
- Mapping file generated successfully
- Manual verification passes

### Phase 3-4 (Integration)
- Changed code correctly mapped to tests
- Test selection reduces execution time by 50%+
- Zero false negatives (missed tests)

### Phase 5 (Reports)
- Report renders correctly
- Users find report actionable
- Coverage gaps clearly identified

### Overall
- Adoption by 3+ teams in production
- Positive user feedback
- Contribution to faster development cycles

---

## Open Questions

1. **JUnit 4 Support** — Should we support JUnit 4, or JUnit Platform only?
2. **TestNG Support** — Priority for TestNG listener?
3. **Kotlin Test Support** — Any special handling needed?
4. **Spock Support** — BDD-style specifications?
5. **Mapping Storage** — Local file vs shared cache (S3, Artifactory)?
6. **CI Integration** — GitHub Actions, GitLab CI examples?

---

## References

- [Async-Profiler](https://github.com/jvm-profiling-tools/async-profiler) — Sampling approach inspiration
- [JFR Documentation](https://docs.oracle.com/en/java/javase/17/jfapi/) — Alternative sampling source
- [JUnit Platform Launcher API](https://junit.org/junit5/docs/current/user-guide/#launcher-api)
- [Gradle Test Filtering](https://docs.gradle.org/current/userguide/java_testing.html#test_filtering)
- [Test Impact Analysis (Microsoft)](https://docs.microsoft.com/en-us/azure/devops/pipelines/test/test-impact-analysis)

---

## Appendix A: Sample Data Structures

### Raw Sample (Phase 1 output)

```json
{
  "timestamp": 1706789012345,
  "testId": "com.example.CalculatorTest#shouldMultiply",
  "threadName": "Test worker",
  "frames": [
    {"class": "com.example.Calculator", "method": "multiply", "line": 47},
    {"class": "com.example.CalculatorTest", "method": "shouldMultiply", "line": 23},
    {"class": "org.junit.platform.engine...", "method": "execute", "line": 0}
  ]
}
```

### Aggregated Mapping (Phase 2 output)

```json
{
  "com.example.Calculator#multiply(int,int)": {
    "totalHits": 365,
    "tests": {
      "com.example.CalculatorTest#shouldMultiply": {
        "depth": 1, 
        "samples": 342,
        "hits": 342
      },
      "com.example.OrderServiceTest#shouldCalculateTotal": {
        "depth": 3, 
        "samples": 23,
        "hits": 23
      }
    }
  }
}
```

### Hot Methods Summary (Phase 2 output)

```json
{
  "hotMethods": [
    {
      "method": "com.example.Validator#validate(Object)",
      "totalHits": 4521,
      "testCount": 23,
      "maxHitsInSingleTest": 1205,
      "testWithMaxHits": "ValidationTest#shouldValidateAll"
    }
  ]
}
```

---

## Appendix B: Depth Calculation

```
Stack (bottom to top):
────────────────────────────────────────────────
org.junit.platform...                    [filtered]
com.example.CalculatorTest#shouldMultiply   ← TEST ANCHOR (depth 0)
com.example.Calculator#calculate            ← depth 1
com.example.Calculator#multiply             ← depth 2
com.example.MathUtils#multiplyExact         ← depth 3
────────────────────────────────────────────────

Depth = distance from test method in call stack
```

---

## Changelog

| Date | Version | Changes |
|------|---------|---------|
| 2025-02-01 | 0.1 | Initial draft |
