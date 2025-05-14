package io.github.surpsg.deltacoverage.gradle.intellij

import io.github.surpsg.deltacoverage.gradle.TestProjects
import io.github.surpsg.deltacoverage.gradle.assertOutputContainsStrings
import io.github.surpsg.deltacoverage.gradle.runDeltaCoverageTaskAndFail
import io.github.surpsg.deltacoverage.gradle.test.GradlePluginTest
import io.github.surpsg.deltacoverage.gradle.test.GradleRunnerInstance
import io.github.surpsg.deltacoverage.gradle.test.ProjectFile
import io.github.surpsg.deltacoverage.gradle.test.RestorableFile
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@GradlePluginTest(TestProjects.SINGLE_MODULE, kts = true)
class KoverPluginVariantTest {

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
    fun `delta-coverage should fail build if coverage rules are violated`() {
        // GIVEN
        applyKoverPlugin()
        buildFile.file.appendText(
            """
            configure<DeltaCoverageConfiguration> {
                coverage {
                    engine = CoverageEngine.INTELLIJ
                    autoApplyPlugin = false
                }
                coverage.engine = CoverageEngine.INTELLIJ
                diffSource.file = "$diffFilePath"
                reportViews {
                    val test by getting {
                        coverageBinaryFiles = files("build/kover/bin-reports/test.ic")
                        violationRules failIfCoverageLessThan 1.0
                    }
                }
                excludeClasses.value(
                    listOf("*/noop/exclude/filter/")
                )
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runDeltaCoverageTaskAndFail()
            .assertOutputContainsStrings(
                "BRANCH: expectedMin=1.0, actual=0.75",
                "LINE: expectedMin=1.0, actual=0.6",
                "INSTRUCTION: expectedMin=1.0, actual=0.6"
            )
    }

    private fun applyKoverPlugin() {
        val pluginDeclaration = """
            id("org.jetbrains.kotlinx.kover") version "$KOVER_PLUGIN_VERSION"
        """.trimIndent()
        with(buildFile.file) {
            val buildFileContent = readText().replace(EXTRA_PLUGINS_PLACEHOLDER, pluginDeclaration)
            writeText(buildFileContent)
        }
    }

    private companion object {
        const val KOVER_PLUGIN_VERSION = "0.9.1"
        const val EXTRA_PLUGINS_PLACEHOLDER = "// {EXTRA_PLUGINS_PLACEHOLDER}"
    }
}
