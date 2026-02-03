package io.github.surpsg.deltacoverage.gradle

import io.github.gwkit.gradleprobe.RestorableFile
import io.github.gwkit.gradleprobe.gradlerunner.runTask
import io.github.gwkit.gradleprobe.junit.GradlePluginTest
import io.github.gwkit.gradleprobe.junit.GradleRunnerInstance
import io.github.gwkit.gradleprobe.junit.ProjectFile
import io.github.gwkit.gradleprobe.junit.RootProjectDir
import io.kotest.matchers.collections.shouldNotBeEmpty
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
                }
            }
        """.trimIndent()
        )

        // WHEN
        val result = gradleRunner.runTask("test", "analyzeTestMapping")

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

        // Check analysis task output
        println(result.output)
    }
}
