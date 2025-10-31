# Delta Coverage Demo

This module is for manual testing and debugging of `delta-coverage-core` on your local machine. It is not published and is intended only for development use.

## Setup

1. Copy the example configuration file:
   ```bash
   cp src/main/resources/demo.yaml.example src/main/resources/demo.yaml
   ```

2. Edit `demo.yaml` with your actual paths:
   - `diffSourceFile`: Path to your diff file
   - `coverageBinaryFiles`: Path to your coverage files (.ic for INTELLIJ or .exec for JACOCO)
   - `classRoots`: Path to compiled classes
   - `sourceFiles`: Path to source code
   - `coverageEngine`: Choose JACOCO or INTELLIJ

## Usage

### Run with default config file

```bash
./gradlew :delta-coverage-demo:run
```

This will use `delta-coverage-demo/src/main/resources/demo.yaml`

### Run with custom config file

```bash
./gradlew :delta-coverage-demo:run --args="/path/to/custom.yaml"
```

## Example Configuration

```yaml
coverageEngine: INTELLIJ
viewName: demo
diffSourceFile: /Users/username/project/changes.diff
coverageBinaryFiles:
  - /Users/username/project/build/coverage/test.ic
classRoots:
  - /Users/username/project/build/classes/kotlin/main
sourceFiles:
  - /Users/username/project/src/main/kotlin
reportDir: build/reports/delta-coverage-demo
reports:
  html: true
  console: true
  markdown: false
  fullCoverage: true
```

## Output

Reports will be generated in the directory specified by `reportDir` (default: `build/reports/delta-coverage-demo`).

You can open the HTML report in a browser:
```bash
open build/reports/delta-coverage-demo/html/index.html
```

## Logging Configuration

Logging is configured via `src/main/resources/logback.xml`. You can control the logging levels for:

- **Delta Coverage core**: `io.github.surpsg.deltacoverage` (default: INFO)
- **Demo application**: `io.github.surpsg.deltacoverage.demo` (default: INFO)
- **Root logger**: All other libraries (default: WARN)

To change logging levels, edit `logback.xml`:

```xml
<!-- Set to DEBUG for verbose output -->
<logger name="io.github.surpsg.deltacoverage" level="DEBUG"/>

<!-- Or set to ERROR to suppress most output -->
<logger name="io.github.surpsg.deltacoverage" level="ERROR"/>
```

Logs are written to:
- **Console**: Standard output
- **File**: `delta-coverage-demo/logs/delta-coverage-demo.log`