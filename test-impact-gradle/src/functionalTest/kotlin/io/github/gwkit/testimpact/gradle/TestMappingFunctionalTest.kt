package io.github.gwkit.testimpact.gradle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.gwkit.gradleprobe.RestorableFile
import io.github.gwkit.gradleprobe.gradlerunner.runTask
import io.github.gwkit.gradleprobe.junit.GradlePluginTest
import io.github.gwkit.gradleprobe.junit.GradleRunnerInstance
import io.github.gwkit.gradleprobe.junit.ProjectFile
import io.github.gwkit.gradleprobe.junit.RootProjectDir
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

@Suppress("UNCHECKED_CAST")
@GradlePluginTest("single-module-test-project")
class TestMappingFunctionalTest {

    @RootProjectDir
    lateinit var rootProjectDir: File

    @ProjectFile("build.gradle.kts")
    lateinit var buildFile: RestorableFile

    @GradleRunnerInstance
    lateinit var gradleRunner: GradleRunner

    @Test
    fun `test mapping should create JFR recording and test events files`() {
        // GIVEN
        buildFile.file.appendText(
            """
            testImpact {
                enabled = true
                includePackages.add("com.java.test")
            }
        """.trimIndent()
        )

        // WHEN
        gradleRunner.runTask("test", "analyzeTestMapping")
            .apply {
                println(output)
            }

        // THEN
        // Check JFR file exists
        val jfrFiles = rootProjectDir.walkTopDown()
            .filter { it.name == "recording.jfr" }
            .toList()
        jfrFiles.shouldNotBeEmpty()

        // Check test-events file exists and contains test class
        val testEventsFiles = rootProjectDir.walkTopDown()
            .filter { it.name == "test-events.txt" }
            .toList()
        testEventsFiles.shouldNotBeEmpty()
        testEventsFiles.first().readText() shouldContain "Class1Test"

        // Check JSON report file
        val jsonFile = rootProjectDir.resolve("build/reports/test-impact/test-mapping.json")
        jsonFile.exists() shouldBe true

        val report: Map<String, Any> = jacksonObjectMapper().readValue(jsonFile)
        report["version"] shouldBe 1
        report["generatedAt"] shouldNotBe null


        val summary = report["summary"] as Map<String, Any>
        summary["totalTests"] shouldBe 1
        (summary["totalMethods"] as Int) shouldBeGreaterThan 0
        (summary["totalSamples"] as Int) shouldBeGreaterThan 0

        val mappings = report["mappings"] as Map<String, Any>
        mappings.keys.shouldNotBeEmpty()

        // Verify output contains Class1 (the production code)
        mappings.keys.any { it.contains("Class1") } shouldBe true
    }
}
