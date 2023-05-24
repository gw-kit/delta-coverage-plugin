package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.test.GradlePluginTest
import io.github.surpsg.deltacoverage.gradle.test.GradleRunnerInstance
import io.github.surpsg.deltacoverage.gradle.test.ProjectFile
import io.github.surpsg.deltacoverage.gradle.test.RestorableFile
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@GradlePluginTest(TestProjects.SINGLE_MODULE)
class DeltaCoverageConfigurationsTest {

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
    fun `delta-coverage should not fail build on printing extension`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
            }
            println(extensions.getByName('deltaCoverageReport'))   
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runTask("tasks", "-m")
            .assertOutputContainsStrings("DeltaCoverageConfiguration")
    }

    @Test
    fun `delta-coverage should fail if classes file collection is empty`() {
        // setup
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                classesDirs = files()
            }
        """.trimIndent()
        )

        // run // assert
        gradleRunner
            .runDeltaCoverageTaskAndFail()
            .assertOutputContainsStrings("'deltaCoverageReport.classesDirs' file collection is empty.")
    }
}
