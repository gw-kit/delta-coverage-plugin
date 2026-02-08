# CI Integration

Run Delta Coverage in your CI pipeline to enforce coverage on every pull request.

## GitHub Actions

=== "Gradle Plugin"

    ```yaml
    name: Build

    on:
      pull_request:
        branches: [main]

    jobs:
      build:
        runs-on: ubuntu-latest
        steps:
          - uses: actions/checkout@v4
            with:
              fetch-depth: 0  # Required for git diff

          - uses: actions/setup-java@v4
            with:
              java-version: '17'
              distribution: 'temurin'

          - name: Run tests with coverage
            run: ./gradlew test deltaCoverage
    ```

=== "CLI"

    ```yaml
    name: Build

    on:
      pull_request:
        branches: [main]

    jobs:
      coverage:
        runs-on: ubuntu-latest
        steps:
          - uses: actions/checkout@v4
            with:
              fetch-depth: 0

          - uses: actions/setup-java@v4
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

!!! warning "fetch-depth: 0"
    Without full git history, the plugin cannot compute the diff against the base branch.

### With PR Comments

Use [delta-coverage-action](https://github.com/gw-kit/delta-coverage-action) to post coverage reports as PR comments:

```yaml
- name: Run tests with coverage
  run: ./gradlew test deltaCoverage
  continue-on-error: true

- name: Post coverage comment
  uses: gw-kit/delta-coverage-action@v1
  with:
    report-path: build/reports/coverage-reports/delta-coverage/test/report.md
```

See [PR Comments](pr-comments.md) for detailed setup.

## GitLab CI

=== "Gradle Plugin"

    ```yaml
    stages:
      - test

    test:
      stage: test
      image: eclipse-temurin:17
      script:
        - ./gradlew test deltaCoverage
      artifacts:
        paths:
          - build/reports/coverage-reports/
        reports:
          coverage_report:
            coverage_format: cobertura
            path: build/reports/coverage-reports/delta-coverage/test/report.xml
    ```

=== "CLI"

    ```yaml
    stages:
      - test

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

### GitLab Coverage Parsing

Add to your job:

```yaml
coverage: '/Total.*?(\d+(?:\.\d+)?)%/'
```

## Jenkins

=== "Gradle Plugin"

    ### Declarative Pipeline

    ```groovy
    pipeline {
        agent any

        stages {
            stage('Test') {
                steps {
                    sh './gradlew test deltaCoverage'
                }
            }
        }

        post {
            always {
                publishHTML(target: [
                    reportDir: 'build/reports/coverage-reports/delta-coverage/test/html',
                    reportFiles: 'index.html',
                    reportName: 'Delta Coverage'
                ])
            }
        }
    }
    ```

=== "CLI"

    ### Declarative Pipeline

    ```groovy
    pipeline {
        agent any

        stages {
            stage('Build') {
                steps {
                    sh './gradlew build'
                }
            }
            stage('Delta Coverage') {
                steps {
                    sh 'git diff origin/main...HEAD > changes.diff'
                    sh '''
                        curl -L -o delta-coverage-cli.jar \
                          https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-cli/3.6.0/delta-coverage-cli-3.6.0.jar
                    '''
                    sh '''
                        java -jar delta-coverage-cli.jar \
                          --engine JACOCO \
                          --diff-file changes.diff \
                          --coverage-binary 'build/**/jacoco/*.exec' \
                          --classes 'build/classes/**/main' \
                          --sources src/main/java \
                          --html --console \
                          --fail-on-violation
                    '''
                }
            }
        }

        post {
            always {
                publishHTML(target: [
                    reportDir: 'build/reports/delta-coverage',
                    reportFiles: 'html/index.html',
                    reportName: 'Delta Coverage'
                ])
            }
        }
    }
    ```

## CircleCI

=== "Gradle Plugin"

    ```yaml
    version: 2.1

    jobs:
      build:
        docker:
          - image: cimg/openjdk:17.0
        steps:
          - checkout
          - run:
              name: Run tests with coverage
              command: ./gradlew test deltaCoverage
          - store_artifacts:
              path: build/reports/coverage-reports
              destination: coverage-reports
    ```

=== "CLI"

    ```yaml
    version: 2.1

    jobs:
      build:
        docker:
          - image: cimg/openjdk:17.0
        steps:
          - checkout
          - run:
              name: Build and test
              command: ./gradlew build
          - run:
              name: Generate diff
              command: git diff origin/main...HEAD > changes.diff
          - run:
              name: Run Delta Coverage
              command: |
                curl -L -o delta-coverage-cli.jar \
                  https://repo1.maven.org/maven2/io/github/gw-kit/delta-coverage-cli/3.6.0/delta-coverage-cli-3.6.0.jar
                java -jar delta-coverage-cli.jar \
                  --engine JACOCO \
                  --diff-file changes.diff \
                  --coverage-binary 'build/**/jacoco/*.exec' \
                  --classes 'build/classes/**/main' \
                  --sources src/main/java \
                  --html --console \
                  --fail-on-violation
          - store_artifacts:
              path: build/reports/delta-coverage
              destination: delta-coverage-report
    ```

## Azure Pipelines

=== "Gradle Plugin"

    ```yaml
    trigger:
      - main

    pool:
      vmImage: 'ubuntu-latest'

    steps:
      - task: Gradle@3
        inputs:
          gradleWrapperFile: 'gradlew'
          tasks: 'test deltaCoverage'
          javaHomeOption: 'JDKVersion'
          jdkVersionOption: '17'

      - task: PublishCodeCoverageResults@2
        inputs:
          codeCoverageTool: 'JaCoCo'
          summaryFileLocation: 'build/reports/coverage-reports/delta-coverage/test/report.xml'
          reportDirectory: 'build/reports/coverage-reports/delta-coverage/test/html'
    ```

=== "CLI"

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

## Configuration for CI

### Compare with PR Base Branch

Most CI systems set environment variables for the base branch:

```kotlin
// build.gradle.kts
val diffBase = providers.gradleProperty("diffBase")
    .orElse(providers.environmentVariable("GITHUB_BASE_REF").map { "refs/remotes/origin/$it" })
    .orElse("refs/remotes/origin/main")

configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource.git.compareWith.set(diffBase)
}
```

### Enable All Report Formats

```kotlin
reports {
    console.set(true)
    html.set(true)
    xml.set(true)      // For CI tools
    markdown.set(true) // For PR comments
}
```

### Stricter Thresholds in CI

```kotlin
val isCI = providers.environmentVariable("CI").isPresent

reportViews {
    val test by getting {
        violationRules {
            val threshold = if (isCI.get()) 0.9 else 0.8
            failIfCoverageLessThan(threshold)
        }
    }
}
```

## Troubleshooting

### "No diff found"

- Ensure `fetch-depth: 0` in checkout
- Verify base branch exists: `git branch -a`
- Check diff source configuration

### Build passes locally but fails in CI

- Different base branches (local vs CI)
- Missing `git fetch` in CI
- Different coverage thresholds
