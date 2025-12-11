package io.github.surpsg.deltacoverage.cli

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.io.PrintWriter
import java.io.StringWriter

class DeltaCoverageCliTest {

    @Test
    fun `should show help message`() {
        // given
        val cli = DeltaCoverageCli()
        val cmd = CommandLine(cli)
        val out = StringWriter()
        cmd.out = PrintWriter(out)

        // when
        val exitCode = cmd.execute("--help")

        // then
        exitCode shouldBe 0
        out.toString() shouldContain "delta-coverage"
        out.toString() shouldContain "--engine"
        out.toString() shouldContain "--diff-file"
        out.toString() shouldContain "--coverage-binary"
    }

    @Test
    fun `should show version`() {
        // given
        val cli = DeltaCoverageCli()
        val cmd = CommandLine(cli)
        val out = StringWriter()
        cmd.out = PrintWriter(out)

        // when
        val exitCode = cmd.execute("--version")

        // then
        exitCode shouldBe 0
        out.toString() shouldContain "delta-coverage-cli"
    }

    @Test
    fun `should return config error when required args are missing`() {
        // given
        val cli = DeltaCoverageCli()
        val cmd = CommandLine(cli)
        val err = StringWriter()
        cmd.err = PrintWriter(err)

        // when
        val exitCode = cmd.execute()

        // then
        exitCode shouldBe DeltaCoverageCli.EXIT_CONFIG_ERROR
    }

    @Test
    fun `should parse engine option`() {
        // given
        val cli = DeltaCoverageCli()
        val cmd = CommandLine(cli)

        // when
        cmd.parseArgs("--engine", "JACOCO")

        // then
        cli.engine shouldBe io.github.surpsg.deltacoverage.CoverageEngine.JACOCO
    }

    @Test
    fun `should parse diff file option`() {
        // given
        val cli = DeltaCoverageCli()
        val cmd = CommandLine(cli)

        // when
        cmd.parseArgs("--diff-file", "changes.diff")

        // then
        cli.diffFile shouldBe "changes.diff"
    }

    @Test
    fun `should parse comma-separated coverage binary files`() {
        // given
        val cli = DeltaCoverageCli()
        val cmd = CommandLine(cli)

        // when
        cmd.parseArgs("--coverage-binary", "file1.exec,file2.exec")

        // then
        cli.coverageBinaryFiles shouldBe listOf("file1.exec", "file2.exec")
    }

    @Test
    fun `should parse report flags`() {
        // given
        val cli = DeltaCoverageCli()
        val cmd = CommandLine(cli)

        // when
        cmd.parseArgs("--html", "--console", "--markdown", "--xml")

        // then
        cli.html shouldBe true
        cli.console shouldBe true
        cli.markdown shouldBe true
        cli.xml shouldBe true
    }

    @Test
    fun `should parse violation options`() {
        // given
        val cli = DeltaCoverageCli()
        val cmd = CommandLine(cli)

        // when
        cmd.parseArgs("--min-coverage", "0.8", "--fail-on-violation")

        // then
        cli.minCoverage shouldBe 0.8
        cli.failOnViolation shouldBe true
    }
}
