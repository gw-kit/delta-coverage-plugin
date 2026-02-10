# PR Comments

Post Delta Coverage reports directly to pull request comments using the [delta-coverage-action](https://github.com/gw-kit/delta-coverage-action).

## Setup

### 1. Enable Markdown Reports

```kotlin
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith("refs/remotes/origin/main")

    reports {
        markdown.set(true)
    }
}
```

### 2. Add GitHub Action

```yaml
name: Build

on:
  pull_request:
    branches: [main]

permissions:
  contents: read
  pull-requests: write  # Required for PR comments

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run tests with coverage
        run: ./gradlew test deltaCoverage

      - name: Post coverage comment
        uses: gw-kit/delta-coverage-action@v1
        with:
          report-path: build/reports/coverage-reports/delta-coverage/test/report.md
```

## How It Works

The action:

1. Posts coverage results to **GitHub Check Runs** with detailed reports
2. Adds a **summary comment** to the PR with links to check runs and progress bars

### PR Comment Example

<h4>📈 Δelta Coverage Check</h4>

<table><tbody><tr><th>Check</th>
<th>Expected</th>
<th>Entity</th>
<th>Actual</th></tr><tr>
<td rowspan=3>🔴 <a href="https://github.com/gw-kit/delta-coverage-plugin/runs/61413672079">Aggregated</a></td>
<td>🎯 91% 🎯</td>
<td><span title="The Java bytecode instructions executed during testing">INSTRUCTION</span></td>
<td><img src="https://progress-bar.xyz/89/?progress_color=C4625A" alt="89%" /></td>
</tr>
<tr>
<td>🎯 90% 🎯</td>
<td><span title="The branches in conditional statements like if, switch, or loops that are executed.">BRANCH</span></td>
<td><img src="https://progress-bar.xyz/72/?progress_color=C4625A" alt="72%" /></td>
</tr>
<tr>
<td>🎯 91% 🎯</td>
<td><span title="The source code lines covered by the tests.">LINE</span></td>
<td><img src="https://progress-bar.xyz/91/?progress_color=7AB56D" alt="91%" /></td>
</tr>
<tr>
<td rowspan=3>🟢 <a href="https://github.com/gw-kit/delta-coverage-plugin/runs/61413672241">FunctionalTest</a></td>
<td>🎯 60% 🎯</td>
<td><span title="The Java bytecode instructions executed during testing">INSTRUCTION</span></td>
<td><img src="https://progress-bar.xyz/82/?progress_color=7AB56D" alt="82%" /></td>
</tr>
<tr>
<td>🎯 50% 🎯</td>
<td><span title="The branches in conditional statements like if, switch, or loops that are executed.">BRANCH</span></td>
<td><img src="https://progress-bar.xyz/51/?progress_color=7AB56D" alt="51%" /></td>
</tr>
<tr>
<td>🎯 60% 🎯</td>
<td><span title="The source code lines covered by the tests.">LINE</span></td>
<td><img src="https://progress-bar.xyz/82/?progress_color=7AB56D" alt="82%" /></td>
</tr>
<tr>
<td rowspan=3>🔴 <a href="https://github.com/gw-kit/delta-coverage-plugin/runs/61413672401">Test</a></td>
<td rowspan=3>🎯 90% 🎯</td>
<td><span title="The Java bytecode instructions executed during testing">INSTRUCTION</span></td>
<td><img src="https://progress-bar.xyz/85/?progress_color=C4625A" alt="85%" /></td>
</tr>
<tr>
<td><span title="The branches in conditional statements like if, switch, or loops that are executed.">BRANCH</span></td>
<td><img src="https://progress-bar.xyz/56/?progress_color=C4625A" alt="56%" /></td>
</tr>
<tr>
<td><span title="The source code lines covered by the tests.">LINE</span></td>
<td><img src="https://progress-bar.xyz/86/?progress_color=C4625A" alt="86%" /></td>
</tr>
</tbody></table>

Each check name links to the detailed GitHub Check Run with the full coverage report.

## Multiple Views

Post comments for multiple views (e.g., unit and integration tests):

```yaml
- name: Post unit test coverage
  uses: gw-kit/delta-coverage-action@v1
  with:
    report-path: build/reports/coverage-reports/delta-coverage/test/report.md
    title: "Unit Test Coverage"

- name: Post integration test coverage
  uses: gw-kit/delta-coverage-action@v1
  with:
    report-path: build/reports/coverage-reports/delta-coverage/integrationTest/report.md
    title: "Integration Test Coverage"
```

## Continue on Failure

Post the comment even if coverage thresholds fail:

```yaml
- name: Run tests with coverage
  run: ./gradlew test deltaCoverage
  continue-on-error: true

- name: Post coverage comment
  uses: gw-kit/delta-coverage-action@v1
  with:
    report-path: build/reports/coverage-reports/delta-coverage/test/report.md
```

## Updating Comments

By default, the action updates an existing comment instead of creating new ones. Each push to the PR updates the same comment with fresh coverage data.

## Permissions

The workflow needs `pull-requests: write` permission:

```yaml
permissions:
  contents: read
  pull-requests: write
```

For workflows triggered by forks, see the [action documentation](https://github.com/gw-kit/delta-coverage-action) for security considerations.
