package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.test.GradlePluginTest
import io.github.surpsg.deltacoverage.gradle.test.GradleRunnerInstance
import io.github.surpsg.deltacoverage.gradle.test.ProjectFile
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

@GradlePluginTest(TestProjects.SINGLE_MODULE)
class DeltaCoverageGradleReleasesTest {

    @ProjectFile("test.diff.file")
    lateinit var diffFilePath: String

    @ProjectFile("build.gradle")
    lateinit var buildFile: File

    @GradleRunnerInstance
    lateinit var gradleRunner: GradleRunner

    @ParameterizedTest
    @ValueSource(
        strings = [
            "5.6",
            "6.7.1",
            "7.6.3",
            "8.5", // the latest Gradle version here
            "8.6-rc-1", // the latest release candidate
        ]
    )
    fun `deltaCoverage task should be completed successfully on Gradle release`(
        gradleVersion: String
    ) {
        // setup
        buildFile.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
            }
        """.trimIndent()
        )

        // run // assert
        gradleRunner
            .withGradleVersion(gradleVersion)
            .runDeltaCoverageTask()
            .assertDeltaCoverageStatusEqualsTo(SUCCESS)
    }

}
