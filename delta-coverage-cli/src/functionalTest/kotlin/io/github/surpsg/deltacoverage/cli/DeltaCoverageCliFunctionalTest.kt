package io.github.surpsg.deltacoverage.cli

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.file.shouldBeADirectory
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class DeltaCoverageCliFunctionalTest {

    @TempDir
    lateinit var tempDir: Path

    private val cliJarPath: String = System.getProperty("cli.jar.path")
        ?: error("System property 'cli.jar.path' is not set")

    @Test
    fun `should load config from file`() {
        // given
        val reportPath = tempDir.resolve("delta-coverage-cli-test-report").toFile()
        val diffFile = tempDir.resolve("test.diff").toFile().apply {
            writeText("")
        }
        val configFile = tempDir.resolve("config.yaml").toFile().apply {
            writeText(
                """
                diffSourceFile: ${diffFile.absolutePath}
                coverageBinaryFiles:
                  - '**/test.exec'
                  - '**/test-data.exec'
                classRoots:
                  - '**/classes/kotlin/main'
                sourceFiles:
                  - 'src/main/**'
                reports:
                  reportDir: ${reportPath.absolutePath}
                """.trimIndent()
            )
        }

        // when
        val result = runCli("--config", configFile.absolutePath)

        // then
        result.exitCode shouldBe 0
        assertSoftly(reportPath) {
            shouldBeADirectory()
            resolve("cli-summary.json").shouldBeAFile()
        }
    }

    private fun runCli(
        vararg args: String,
    ): CliResult {
        val coverageJvmArgsContent: String = System.getProperty("io.github.gwkit.coverjet.test-kit")
            ?.let(::File)
            ?.readText() ?: ""
        val coverageArgsList: List<String> = coverageJvmArgsContent.substringAfter("=").split(" ")
        val command: List<String> = listOf("java") + coverageArgsList + listOf("-jar", cliJarPath) + args

        return with(ProcessBuilder(command).redirectErrorStream(true).start()) {
            if (waitFor(30, TimeUnit.SECONDS)) {
                CliResult(
                    exitCode = exitValue(),
                    output = inputStream.bufferedReader().readText(),
                )
            } else {
                destroyForcibly()
                error("CLI process timed out")
            }
        }
    }

    private data class CliResult(
        val exitCode: Int,
        val output: String
    )
}
