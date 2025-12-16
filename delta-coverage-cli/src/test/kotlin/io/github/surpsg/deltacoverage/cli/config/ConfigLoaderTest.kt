package io.github.surpsg.deltacoverage.cli.config

import io.github.surpsg.deltacoverage.CoverageEngine
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ConfigLoaderTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should load config from YAML file`() {
        // given
        val configFile = tempDir.resolve("config.yaml").toFile()
        configFile.writeText(
            """
            coverageEngine: JACOCO
            viewName: test-view
            diffSourceFile: changes.diff
            coverageBinaryFiles:
              - build/jacoco/test.exec
            classRoots:
              - build/classes/java/main
            sourceFiles:
              - src/main/java
            excludeClasses:
              - "**/*Test*"
            reports:
              reportDir: build/reports
              html: true
              console: true
              markdown: false
              xml: false
              fullCoverage: false
            violationRules:
              minCoverage: 0.8
              failOnViolation: true
            """.trimIndent()
        )

        // when
        val config = ConfigLoader.loadFromFile(configFile)

        // then
        config.coverageEngine shouldBe CoverageEngine.JACOCO
        config.viewName shouldBe "test-view"
        config.diffSourceFile shouldBe "changes.diff"
        config.coverageBinaryFiles shouldBe listOf("build/jacoco/test.exec")
        config.classRoots shouldBe listOf("build/classes/java/main")
        config.sourceFiles shouldBe listOf("src/main/java")
        config.excludeClasses shouldBe listOf("**/*Test*")
        config.reports.reportDir shouldBe "build/reports"
        config.reports.html shouldBe true
        config.reports.console shouldBe true
        config.reports.markdown shouldBe false
        config.reports.xml shouldBe false
        config.reports.fullCoverage shouldBe false
        config.violationRules.minCoverage shouldBe 0.8
        config.violationRules.failOnViolation shouldBe true
    }

    @Test
    fun `should throw exception when file does not exist`() {
        // given
        val nonExistentFile = File(tempDir.toFile(), "non-existent.yaml")

        // when & then
        val exception = shouldThrow<IllegalArgumentException> {
            ConfigLoader.loadFromFile(nonExistentFile)
        }
        exception.message shouldContain "Configuration file not found"
    }

    @Test
    fun `should throw exception when path is directory`() {
        // given
        val directory = tempDir.resolve("directory").toFile().apply { mkdir() }

        // when & then
        val exception = shouldThrow<IllegalArgumentException> {
            ConfigLoader.loadFromFile(directory)
        }
        exception.message shouldContain "not a file"
    }

    @Test
    fun `should use default values for missing fields`() {
        // given
        val configFile = tempDir.resolve("minimal.yaml").toFile()
        configFile.writeText(
            """
            coverageEngine: INTELLIJ
            diffSourceFile: diff.patch
            """.trimIndent()
        )

        // when
        val config = ConfigLoader.loadFromFile(configFile)

        // then
        config.coverageEngine shouldBe CoverageEngine.INTELLIJ
        config.diffSourceFile shouldBe "diff.patch"
        config.viewName shouldBe "cli"
        config.coverageBinaryFiles shouldBe emptyList()
        config.reports.html shouldBe true
        config.reports.console shouldBe true
        config.violationRules.minCoverage shouldBe null
        config.violationRules.failOnViolation shouldBe false
    }

    @Test
    fun `should load config from JSON file`() {
        // given
        val configFile = tempDir.resolve("config.json").toFile()
        configFile.writeText(
            """
            {
              "coverageEngine": "JACOCO",
              "viewName": "test-view",
              "diffSourceFile": "changes.diff",
              "coverageBinaryFiles": ["build/jacoco/test.exec"],
              "classRoots": ["build/classes/java/main"],
              "sourceFiles": ["src/main/java"],
              "excludeClasses": ["**/*Test*"],
              "reports": {
                "reportDir": "build/reports",
                "html": true,
                "console": true,
                "markdown": false,
                "xml": false,
                "fullCoverage": false
              },
              "violationRules": {
                "minCoverage": 0.8,
                "failOnViolation": true
              }
            }
            """.trimIndent()
        )

        // when
        val config = ConfigLoader.loadFromFile(configFile)

        // then
        config.coverageEngine shouldBe CoverageEngine.JACOCO
        config.viewName shouldBe "test-view"
        config.diffSourceFile shouldBe "changes.diff"
        config.coverageBinaryFiles shouldBe listOf("build/jacoco/test.exec")
        config.classRoots shouldBe listOf("build/classes/java/main")
        config.sourceFiles shouldBe listOf("src/main/java")
        config.excludeClasses shouldBe listOf("**/*Test*")
        config.reports.reportDir shouldBe "build/reports"
        config.reports.html shouldBe true
        config.reports.console shouldBe true
        config.reports.markdown shouldBe false
        config.reports.xml shouldBe false
        config.reports.fullCoverage shouldBe false
        config.violationRules.minCoverage shouldBe 0.8
        config.violationRules.failOnViolation shouldBe true
    }

    @Test
    fun `should use default values for missing fields in JSON`() {
        // given
        val configFile = tempDir.resolve("minimal.json").toFile()
        configFile.writeText(
            """
            {
              "coverageEngine": "INTELLIJ",
              "diffSourceFile": "diff.patch"
            }
            """.trimIndent()
        )

        // when
        val config = ConfigLoader.loadFromFile(configFile)

        // then
        config.coverageEngine shouldBe CoverageEngine.INTELLIJ
        config.diffSourceFile shouldBe "diff.patch"
        config.viewName shouldBe "cli"
        config.coverageBinaryFiles shouldBe emptyList()
        config.reports.html shouldBe true
        config.reports.console shouldBe true
        config.violationRules.minCoverage shouldBe null
        config.violationRules.failOnViolation shouldBe false
    }

    @Test
    fun `should load config from yml file`() {
        // given
        val configFile = tempDir.resolve("config.yml").toFile()
        configFile.writeText(
            """
            coverageEngine: JACOCO
            diffSourceFile: changes.diff
            """.trimIndent()
        )

        // when
        val config = ConfigLoader.loadFromFile(configFile)

        // then
        config.coverageEngine shouldBe CoverageEngine.JACOCO
        config.diffSourceFile shouldBe "changes.diff"
    }

    @Test
    fun `should throw exception for unsupported file format`() {
        // given
        val configFile = tempDir.resolve("config.xml").toFile()
        configFile.writeText("<config/>")

        // when & then
        val exception = shouldThrow<IllegalStateException> {
            ConfigLoader.loadFromFile(configFile)
        }
        exception.message shouldContain "Unsupported config file format"
        exception.message shouldContain "xml"
    }
}
