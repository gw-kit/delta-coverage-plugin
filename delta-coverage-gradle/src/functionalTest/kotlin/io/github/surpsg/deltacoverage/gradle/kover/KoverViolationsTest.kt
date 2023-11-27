package io.github.surpsg.deltacoverage.gradle.kover

import io.github.surpsg.deltacoverage.gradle.assertOutputContainsStrings
import io.github.surpsg.deltacoverage.gradle.runDeltaCoverageTaskAndFail
import io.github.surpsg.deltacoverage.gradle.test.GradlePluginTest
import io.github.surpsg.deltacoverage.gradle.test.GradleRunnerInstance
import io.github.surpsg.deltacoverage.gradle.test.ProjectFile
import io.github.surpsg.deltacoverage.gradle.test.RestorableFile
import io.github.surpsg.deltacoverage.gradle.test.RootProjectDir
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@GradlePluginTest("kover-single-module", kts = true)
class KoverViolationsTest {

    @RootProjectDir
    lateinit var rootProjectDir: File

    @ProjectFile("test.diff.file")
    lateinit var diffFilePath: String

    @ProjectFile("build.gradle.kts")
    lateinit var buildFile: RestorableFile

    @GradleRunnerInstance
    lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun beforeEach() {
        buildFile.restoreOriginContent()
    }

    @Test
    fun `delta-coverage should fail build if coverage x are violated`() {
        // GIVEN
        buildFile.file.appendText(
            """
            configure<DeltaCoverageConfiguration> {
                coverageEngine = CoverageEngine.INTELLIJ
                diffSource.file.set("$diffFilePath")
                
                violationRules failIfCoverageLessThan 1.0
            }
        """.trimIndent()
        )

        // disable jacoco auto-apply
        rootProjectDir.resolve("gradle.properties").appendText(
            """
            io.github.surpsg.delta-coverage.auto-apply-jacoco=false // TODO
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runDeltaCoverageTaskAndFail(printLogs = true)
            .assertOutputContainsStrings(
                "BRANCH: expectedMin=1.0, actual=0.5",
                "LINE: expectedMin=1.0, actual=0.6",
                "INSTRUCTION: expectedMin=1.0, actual=0.5"
            )
    }
}
