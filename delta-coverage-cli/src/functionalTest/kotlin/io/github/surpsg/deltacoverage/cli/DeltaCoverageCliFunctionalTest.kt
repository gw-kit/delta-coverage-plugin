package io.github.surpsg.deltacoverage.cli

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
    fun `should display help message when run with help flag`() {
        // when
        val result = runCli("--help")

        // then
        result.exitCode shouldBe 0
        assertSoftly(result.output) {
            shouldContain("delta-coverage")
            shouldContain("--diff-file")
            shouldContain("--coverage-binary")
            shouldContain("--classes")
            shouldContain("--sources")
        }
    }

    @Test
    fun `should load config from file`() {
        // given
        val configFile = tempDir.resolve("config.yaml").toFile()
        configFile.writeText(
            """
            diffSourceFile: test.diff
            coverageBinaryFiles:
              - test.exec
            classRoots:
              - classes
            sourceFiles:
              - src
            """.trimIndent()
        )

        // when
        val result = runCli("--config", configFile.absolutePath)

        // then
        result.exitCode shouldBe 3
        result.output shouldContain "Runtime error"
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
