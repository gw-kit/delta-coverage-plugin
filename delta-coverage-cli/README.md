# Delta Coverage CLI

A standalone command-line interface for running delta coverage analysis without requiring the Gradle plugin.
This is useful for CI/CD pipelines where you cannot inject the Gradle plugin into build scripts.

## Installation

Download the latest JAR from [Maven Central](https://search.maven.org/artifact/io.github.gw-kit/delta-coverage-cli)
or [GitHub Releases](https://github.com/gw-kit/delta-coverage-plugin/releases).

```bash
# Download from Maven Central
curl -L -o delta-coverage-cli.jar \
  https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-cli/3.6.0/delta-coverage-cli-3.6.0.jar
```

## Quick Start

```bash
# Generate diff file
git diff origin/main...HEAD > changes.diff

# Run delta coverage
java -jar delta-coverage-cli.jar \
  --engine JACOCO \
  --diff-file changes.diff \
  --coverage-binary build/jacoco/test.exec \
  --classes build/classes/java/main \
  --sources src/main/java \
  --html --console
```

## Usage

```
Usage: delta-coverage [-hvV] [--console] [--fail-on-violation]
                      [--full-coverage] [--html] [--markdown] [--xml]
                      [-c=<configFile>] [-e=<engine>] [-f=<diffFile>]
                      [--min-coverage=<minCoverage>] [-o=<reportDir>]
                      [--view-name=<viewName>] [--classes=<classRoots>...]
                      [--coverage-binary=<coverageBinaryFiles>...]
                      [--exclude=<excludeClasses>...] [-s=<sourceFiles>...]

Computes code coverage of new/modified code based on a provided diff.

Options:
  -c, --config=<configFile>     Path to configuration file (YAML or JSON)
  -e, --engine=<engine>         Coverage engine: JACOCO or INTELLIJ
  -f, --diff-file=<diffFile>    Path to diff file
      --coverage-binary=<files> Coverage binary files or glob pattern
      --classes=<dirs>          Class directories or glob pattern
  -s, --sources=<dirs>          Source directories (comma-separated or glob)
      --exclude=<patterns>      Class exclusion patterns (comma-separated)
  -o, --report-dir=<dir>        Output directory for reports
      --html                    Generate HTML report
      --xml                     Generate XML report
      --console                 Generate console report
      --markdown                Generate markdown report
      --full-coverage           Include full coverage in reports
      --min-coverage=<ratio>    Minimum coverage ratio (0.0-1.0)
      --fail-on-violation       Exit with error if coverage below threshold
      --view-name=<name>        Name for the coverage view
  -v, --verbose                 Enable verbose output
  -V, --version                 Print version information
  -h, --help                    Show help message
```

## Glob Pattern Support

File arguments support glob patterns for automatic file discovery:

```bash
java -jar delta-coverage-cli.jar \
  --engine JACOCO \
  --diff-file changes.diff \
  --coverage-binary 'build/**/jacoco/*.exec' \
  --classes 'build/classes/**/main' \
  --sources 'src/main/java,src/main/kotlin' \
  --html --console
```

## Configuration File

For complex configurations, use a YAML or JSON file.

### YAML Format

```yaml
# delta-coverage.yaml
coverageEngine: JACOCO
viewName: cli-run
diffSourceFile: changes.diff
coverageBinaryFiles:
  - build/jacoco/test.exec
  - "build/**/jacoco/*.exec"  # Glob patterns supported
classRoots:
  - build/classes/java/main
sourceFiles:
  - src/main/java
  - src/main/kotlin
excludeClasses:
  - "**/*Test*"
reports:
  reportDir: build/reports/delta-coverage
  html: true
  xml: false
  console: true
  markdown: false
  fullCoverage: false
violationRules:
  minCoverage: 0.8
  failOnViolation: true
```

### JSON Format

```json
{
  "coverageEngine": "JACOCO",
  "viewName": "cli-run",
  "diffSourceFile": "changes.diff",
  "coverageBinaryFiles": [
    "build/jacoco/test.exec",
    "build/**/jacoco/*.exec"
  ],
  "classRoots": ["build/classes/java/main"],
  "sourceFiles": ["src/main/java", "src/main/kotlin"],
  "excludeClasses": ["**/*Test*"],
  "reports": {
    "reportDir": "build/reports/delta-coverage",
    "html": true,
    "xml": false,
    "console": true,
    "markdown": false,
    "fullCoverage": false
  },
  "violationRules": {
    "minCoverage": 0.8,
    "failOnViolation": true
  }
}
```

### Running with Config File

```bash
java -jar delta-coverage-cli.jar --config delta-coverage.yaml
# or
java -jar delta-coverage-cli.jar --config delta-coverage.json
```

Override config file values with CLI arguments:

```bash
java -jar delta-coverage-cli.jar --config delta-coverage.yaml --diff-file pr.diff
```

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Coverage violation (when `--fail-on-violation` is set) |
| 2 | Configuration error (missing required arguments) |
| 3 | Runtime error |

## CI/CD Examples

### GitHub Actions

```yaml
jobs:
  coverage:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build and test
        run: ./gradlew build

      - name: Generate diff
        run: git diff origin/${{ github.base_ref }}...HEAD > changes.diff

      - name: Download Delta Coverage CLI
        run: |
          curl -L -o delta-coverage-cli.jar \
            https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-cli/3.6.0/delta-coverage-cli-3.6.0.jar

      - name: Run Delta Coverage
        run: |
          java -jar delta-coverage-cli.jar \
            --engine JACOCO \
            --diff-file changes.diff \
            --coverage-binary 'build/**/jacoco/*.exec' \
            --classes 'build/classes/**/main' \
            --sources src/main/java \
            --html --console \
            --min-coverage 0.8 \
            --fail-on-violation

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: delta-coverage-report
          path: build/reports/delta-coverage
```

### Azure DevOps

```yaml
trigger:
  - main

pool:
  vmImage: 'ubuntu-latest'

steps:
  - task: JavaToolInstaller@0
    inputs:
      versionSpec: '17'
      jdkArchitectureOption: 'x64'
      jdkSourceOption: 'PreInstalled'

  - script: ./gradlew build
    displayName: 'Build and test'

  - script: git diff origin/main...HEAD > changes.diff
    displayName: 'Generate diff'

  - script: |
      curl -L -o delta-coverage-cli.jar \
        https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-cli/3.6.0/delta-coverage-cli-3.6.0.jar
      java -jar delta-coverage-cli.jar \
        --engine JACOCO \
        --diff-file changes.diff \
        --coverage-binary '$(Build.SourcesDirectory)/build/**/jacoco/*.exec' \
        --classes '$(Build.SourcesDirectory)/build/classes/**/main' \
        --sources $(Build.SourcesDirectory)/src/main/java \
        --report-dir $(Build.ArtifactStagingDirectory)/delta-coverage \
        --html --console \
        --fail-on-violation
    displayName: 'Run Delta Coverage'

  - task: PublishBuildArtifacts@1
    condition: always()
    inputs:
      pathToPublish: '$(Build.ArtifactStagingDirectory)/delta-coverage'
      artifactName: 'delta-coverage-report'
```

### GitLab CI

```yaml
delta-coverage:
  stage: test
  image: eclipse-temurin:17-jdk
  script:
    - ./gradlew build
    - git diff origin/main...HEAD > changes.diff
    - |
      curl -L -o delta-coverage-cli.jar \
        https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-cli/3.6.0/delta-coverage-cli-3.6.0.jar
    - |
      java -jar delta-coverage-cli.jar \
        --engine JACOCO \
        --diff-file changes.diff \
        --coverage-binary 'build/**/jacoco/*.exec' \
        --classes 'build/classes/**/main' \
        --sources src/main/java \
        --html --console \
        --min-coverage 0.8 \
        --fail-on-violation
  artifacts:
    when: always
    paths:
      - build/reports/delta-coverage
    expire_in: 1 week
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/gw-kit/delta-coverage-plugin.git
cd delta-coverage-plugin

# Build the CLI JAR
./gradlew :delta-coverage-cli:shadowJar

# The JAR is located at:
# delta-coverage-cli/build/libs/delta-coverage-cli-<version>.jar
```
