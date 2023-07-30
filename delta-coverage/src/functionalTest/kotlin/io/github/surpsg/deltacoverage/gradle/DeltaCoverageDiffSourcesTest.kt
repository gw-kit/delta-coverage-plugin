package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.git.buildGitRepository
import io.github.surpsg.deltacoverage.gradle.resources.getResourceFile
import io.github.surpsg.deltacoverage.gradle.test.GradlePluginTest
import io.github.surpsg.deltacoverage.gradle.test.GradleRunnerInstance
import io.github.surpsg.deltacoverage.gradle.test.ProjectFile
import io.github.surpsg.deltacoverage.gradle.test.RestorableFile
import io.github.surpsg.deltacoverage.gradle.test.RootProjectDir
import io.github.surpsg.deltacoverage.gradle.test.mockserver.MockHttpServer
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@GradlePluginTest(TestProjects.SINGLE_MODULE)
class DeltaCoverageDiffSourcesTest {

    @RootProjectDir
    lateinit var rootProjectDir: File

    @ProjectFile("build.gradle")
    lateinit var buildFile: RestorableFile

    @ProjectFile("test.diff.file")
    lateinit var diffFilePath: String

    @GradleRunnerInstance
    lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun beforeEach() {
        buildFile.restoreOriginContent()
    }

    @Test
    fun `delta-coverage should get diff info by file`() {
        // setup
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                violationRules {
                    minInstructions.set(1d)
                    failOnViolation.set(true)
                }
            }
            """.trimIndent()
        )

        MockHttpServer(MOCK_SERVER_PORT, File(diffFilePath).readText()).use {
            // run // assert
            gradleRunner
                .runDeltaCoverageTaskAndFail()
                .assertOutputContainsStrings("instructions covered ratio is 0.5, but expected minimum is 1")
        }
    }

    @Test
    fun `delta-coverage should get diff info by url`() {
        // setup
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.url.set('http://localhost:${MOCK_SERVER_PORT}/')
                violationRules {
                    minInstructions.set(1d)
                    failOnViolation.set(true)
                }
            }
            """.trimIndent()
        )

        MockHttpServer(MOCK_SERVER_PORT, File(diffFilePath).readText()).use {
            // run // assert
            gradleRunner
                .runDeltaCoverageTaskAndFail()
                .assertOutputContainsStrings("instructions covered ratio is 0.5, but expected minimum is 1")
        }
    }

    @Test
    fun `delta-coverage should use git to generate diff`() {
        // setup
        prepareTestProjectWithGit()

        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.git.compareWith 'HEAD'
                
                violationRules.failIfCoverageLessThan(0.7d)
            }
            
            """.trimIndent()
        )

        // run // assert
        gradleRunner
            .runDeltaCoverageTaskAndFail(printLogs = true)
            .assertOutputContainsStrings(
                "instructions covered ratio is 0.5, but expected minimum is 0.7",
                "branches covered ratio is 0.5, but expected minimum is 0.7",
                "lines covered ratio is 0.6, but expected minimum is 0.7"
            )
    }

    private fun prepareTestProjectWithGit() {
        rootProjectDir.resolve(".gitignore").apply {
            appendText("\n*")
            appendText("\n!*.java")
            appendText("\n!gitignore")
            appendText("\n!*/")
        }
        buildGitRepository(rootProjectDir).use { git ->
            git.add().addFilepattern(".").call()
            git.commit().setMessage("Add all").call()

            val oldVersionFile = "src/main/java/com/java/test/Class1.java"
            val targetFile = rootProjectDir.resolve(oldVersionFile)
            getResourceFile<DeltaCoverageDiffSourcesTest>("git-diff-source-test-files/Class1GitTest.java")
                .copyTo(targetFile, true)

            git.add().addFilepattern(oldVersionFile).call()
            git.commit().setMessage("Added old file version").call()

            getResourceFile<DeltaCoverageDiffSourcesTest>("${TestProjects.SINGLE_MODULE}/src")
                .copyRecursively(rootProjectDir.resolve("src"), true)
            git.add().addFilepattern(".").call()
        }
    }

    companion object {
        private const val MOCK_SERVER_PORT = 8888
    }
}
