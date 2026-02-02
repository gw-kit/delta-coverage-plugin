package io.github.surpsg.deltacoverage.gradle

import io.github.gwkit.gradleprobe.RestorableFile
import io.github.gwkit.gradleprobe.gradlerunner.runTask
import io.github.gwkit.gradleprobe.junit.GradlePluginTest
import io.github.gwkit.gradleprobe.junit.GradleRunnerInstance
import io.github.gwkit.gradleprobe.junit.ProjectFile
import io.github.gwkit.gradleprobe.junit.RootProjectDir
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
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
    fun `test mapping DSL should be configurable and test task should be configured`() {
        // GIVEN
        val samplesFile = "build/reports/delta-coverage/test-samples.json"
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                testMapping {
                    enabled = true
                    sampling {
                        intervalMs = 1
                        maxDepth = 50
                    }
                    output {
                        samplesFile = '$samplesFile'
                    }
                }
            }

            // Verify the configuration is applied by checking test task's systemProperties
            // Note: systemProperty() sets properties for the forked test JVM, not the Gradle JVM
            tasks.named('test') {
                doFirst {
                    def props = systemProperties
                    def enabledProp = props['delta.coverage.sampling.enabled']
                    def intervalProp = props['delta.coverage.sampling.intervalMs']
                    def maxDepthProp = props['delta.coverage.sampling.maxDepth']
                    def outputProp = props['delta.coverage.sampling.outputFile']

                    println "TEST_MAPPING_CONFIG: enabled=${'$'}enabledProp, intervalMs=${'$'}intervalProp, maxDepth=${'$'}maxDepthProp"
                    println "TEST_MAPPING_OUTPUT: ${'$'}outputProp"

                    assert enabledProp == 'true' : "Expected enabled=true but got ${'$'}enabledProp"
                    assert intervalProp == '1' : "Expected intervalMs=1 but got ${'$'}intervalProp"
                    assert maxDepthProp == '50' : "Expected maxDepth=50 but got ${'$'}maxDepthProp"
                    assert outputProp?.contains('test-samples.json') : "Expected output path to contain test-samples.json"
                }
            }
        """.trimIndent()
        )

        // WHEN
        val result = gradleRunner.runTask("test", "--info")

        // THEN - verify the configuration was applied by checking the output
        assertSoftly {
            result.output.shouldContain("TEST_MAPPING_CONFIG: enabled=true, intervalMs=1, maxDepth=50")
            result.output.shouldContain("test-samples.json")
        }

        // Note: In TestKit, the listener JARs may not be found, so samples file may not be created
        // This test verifies DSL configuration is correctly passed to test task
    }
}
