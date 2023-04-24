package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_TASK
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DeltaCoverageGradleReleasesTest : BaseDeltaCoverageTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle",
        "test.diff.file"
    )

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @ParameterizedTest
    @ValueSource(strings = ["5.1", "5.3", "6.7.1", "7.4.2", "8.1"])
    fun `deltaCoverage task should be completed successfully on Gradle release`(
        gradleVersion: String
    ) {
        // setup
        buildFile.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                jacocoExecFiles = fileTree('build') { include '*/**/*.exec' }
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.withGradleVersion(gradleVersion).runTask(DELTA_COVERAGE_TASK)

        // assert
        result.assertDeltaCoverageStatusEqualsTo(SUCCESS)
    }

}
