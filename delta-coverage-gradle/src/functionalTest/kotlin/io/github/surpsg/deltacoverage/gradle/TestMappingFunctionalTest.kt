package io.github.surpsg.deltacoverage.gradle

import io.github.gwkit.gradleprobe.RestorableFile
import io.github.gwkit.gradleprobe.gradlerunner.runTask
import io.github.gwkit.gradleprobe.junit.GradlePluginTest
import io.github.gwkit.gradleprobe.junit.GradleRunnerInstance
import io.github.gwkit.gradleprobe.junit.ProjectFile
import io.github.gwkit.gradleprobe.junit.RootProjectDir
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@GradlePluginTest(TestProjects.SINGLE_MODULE, kts = false)
class TestMappingFunctionalTest {

    @RootProjectDir
    lateinit var rootProjectDir: File

    @ProjectFile("test.diff.file")
    lateinit var diffFilePath: String

    @ProjectFile("build.gradle")
    lateinit var buildFile: RestorableFile

    @GradleRunnerInstance
    lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun beforeEach() {
        buildFile.restoreOriginContent()
    }

    @Test
    fun `test mapping should create JFR recording and test events files`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                testMapping {
                    enabled = true
                    includePackages = ['com.java.test']
                }
            }
        """.trimIndent()
        )

        // WHEN
        gradleRunner.runTask("test", "analyzeTestMapping")

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
        val jsonFile = rootProjectDir.resolve("build/reports/delta-coverage/test-mapping.json")
        jsonFile.exists() shouldBe true

        println(jsonFile.readText())

        val report: Map<String, Any> = jacksonObjectMapper().readValue(jsonFile)
        report["version"] shouldBe 1
        report["generatedAt"] shouldNotBe null

        @Suppress("UNCHECKED_CAST")
        val summary = report["summary"] as Map<String, Any>
        summary["totalTests"] shouldBe 1
        (summary["totalMethods"] as Int) shouldBeGreaterThan 0
        (summary["totalSamples"] as Int) shouldBeGreaterThan 0

        @Suppress("UNCHECKED_CAST")
        val mappings = report["mappings"] as Map<String, Any>
        mappings.keys.shouldNotBeEmpty()

        // Verify output contains Class1 (the production code)
        mappings.keys.any { it.contains("Class1") } shouldBe true
    }
}
