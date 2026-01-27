package io.github.surpsg.deltacoverage.cli.report

import io.github.surpsg.deltacoverage.cli.CoverageViolationException
import io.github.surpsg.deltacoverage.cli.config.CliConfig
import io.github.surpsg.deltacoverage.exception.CoverageViolatedException
import io.github.surpsg.deltacoverage.report.DeltaReportGeneratorFacade
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CliReportRunnerTest {

    @Test
    fun `should generate reports successfully`() {
        // given
        val config = createTestConfig()
        val mockFacade = mockk<DeltaReportGeneratorFacade>(relaxed = true)
        val runner = CliReportRunner { mockFacade }

        // when & then
        shouldNotThrowAny {
            runner.run(config)
        }

        verify { mockFacade.generateReports(any()) }
    }

    @Test
    fun `should throw CoverageViolationException when failOnViolation is true and violation occurs`() {
        // given
        val config = createTestConfig(failOnViolation = true)
        val violationMessage = "Line coverage is below threshold"
        val runner = CliReportRunner {
            mockk<DeltaReportGeneratorFacade> {
                every { generateReports(any()) } throws CoverageViolatedException(violationMessage)
            }
        }

        // when & then
        val exception = shouldThrow<CoverageViolationException> {
            runner.run(config)
        }
        exception.message shouldContain violationMessage
    }

    @Test
    fun `should not throw when failOnViolation is false and violation occurs`() {
        // given
        val config = createTestConfig(failOnViolation = false)
        val runner = CliReportRunner {
            mockk<DeltaReportGeneratorFacade> {
                every { generateReports(any()) } throws CoverageViolatedException("Coverage violation")
            }
        }

        // when & then
        shouldNotThrowAny {
            runner.run(config)
        }
    }

    private fun createTestConfig(failOnViolation: Boolean = false) = CliConfig(
        diffSourceFile = "test.diff",
        violationRules = CliConfig.ViolationRulesConfig(
            failOnViolation = failOnViolation
        )
    )
}
