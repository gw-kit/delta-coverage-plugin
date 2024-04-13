package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.Reporter
import com.intellij.rt.coverage.report.api.Filters
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.intellij.coverage.NamedReportLoadStrategy
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.equals.Equality
import io.kotest.equals.EqualityResult
import io.kotest.equals.ReflectionUsingFieldsEquality
import io.kotest.equals.SimpleEqualityResult
import io.kotest.equals.SimpleEqualityResultDetail
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.sequences.shouldBeEmpty
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty

class CoverageReportFactoryTest {

    @Test
    fun `reportBuildersBy should return empty iterable if no enabled reports`() {
        // GIVEN
        val config = ReportsConfig {
            html = ReportConfig { enabled = false }
            xml = ReportConfig { enabled = false }
            csv = ReportConfig { enabled = false }
            console = ReportConfig { enabled = false }
            markdown = ReportConfig { enabled = false }
        }

        // WHEN
        val actualBuilders: Sequence<ReportBuilder> = CoverageReportFactory.reportBuildersBy(
            config,
            listOf(anyReportLoadStrategy())
        )

        // THEN
        actualBuilders.shouldBeEmpty()
    }

    @Test
    fun `reportBuildersBy should return empty iterable if no report loaders`() {
        // GIVEN
        val config = ReportsConfig {
            html = ReportConfig { enabled = true }
            xml = ReportConfig { enabled = true }
            csv = ReportConfig { enabled = true }
            console = ReportConfig { enabled = true }
            markdown = ReportConfig { enabled = true }
        }

        // WHEN
        val actualBuilders: Sequence<ReportBuilder> = CoverageReportFactory.reportBuildersBy(
            config,
            emptyList()
        )

        // THEN
        actualBuilders.shouldBeEmpty()
    }

    @Test
    fun `reportBuildersBy should throw if unsupported csv report is enabled`() {
        // GIVEN
        val config = ReportsConfig {
            csv = ReportConfig { enabled = true }
        }
        val reportLoadStrategies = listOf(anyReportLoadStrategy())

        // WHEN // THEN
        shouldThrow<IllegalStateException> {
            CoverageReportFactory.reportBuildersBy(config, reportLoadStrategies).toList()
        }
    }

    @Test
    fun `reportBuildersBy should return report builders`() {
        // GIVEN
        val config = ReportsConfig {
            html = ReportConfig { enabled = true }
            xml = ReportConfig { enabled = true }
            console = ReportConfig { enabled = true }
            markdown = ReportConfig { enabled = true }
        }
        val reportLoadStrategy = anyReportLoadStrategy()

        // WHEN
        val actualBuilders: Sequence<ReportBuilder> = CoverageReportFactory.reportBuildersBy(
            config,
            listOf(reportLoadStrategy)
        )

        // THEN
        assertSoftly(actualBuilders.toList()) {
            shouldHaveSize(4)

            shouldContain(
                HtmlReportBuilder(
                    REPORT_NAME,
                    REPORT_BOUND,
                    config,
                    Reporter(reportLoadStrategy.reportLoadStrategy)
                ),
                EqualByFields.fromFields(
                    HtmlReportBuilder::reportName,
                    HtmlReportBuilder::reportBound,
                )
            )

            shouldContain(
                XmlReportBuilder(
                    REPORT_BOUND,
                    config,
                    Reporter(reportLoadStrategy.reportLoadStrategy)
                ),
                EqualByFields.fromFields(XmlReportBuilder::reportBound)
            )

            shouldContain(
                ConsoleReportBuilder(
                    REPORT_BOUND,
                    Reporter(reportLoadStrategy.reportLoadStrategy),
                ),
                EqualByFields.fromFields(ConsoleReportBuilder::reportBound)
            )

            shouldContain(
                MarkdownReportBuilder(
                    REPORT_BOUND,
                    config,
                    Reporter(reportLoadStrategy.reportLoadStrategy),
                ),
                EqualByFields.fromFields(MarkdownReportBuilder::reportBound)
            )
        }
    }

    private fun anyReportLoadStrategy() = NamedReportLoadStrategy(
        reportName = REPORT_NAME,
        reportBound = REPORT_BOUND,
        reportLoadStrategy = ReportLoadStrategy.RawReportLoadStrategy(
            emptyList(),
            emptyList(),
            emptyList(),
            Filters.EMPTY
        ),
    )

    class EqualByFields<T : Any>(
        private val fields: Array<out KProperty<*>>
    ) : Equality<T> {

        override fun name(): String = "Objects equal by instance and fields"

        override fun verify(actual: T, expected: T): EqualityResult {
            return if (actual::class.isInstance(expected)) {
                ReflectionUsingFieldsEquality<T>(fields).verify(actual, expected)
            } else {
                SimpleEqualityResult(
                    false,
                    SimpleEqualityResultDetail {
                        "Not the same type: actual=${actual::class}, expected=${expected::class}"
                    }
                )
            }
        }

        companion object {
            fun <T : Any> fromFields(vararg fields: KProperty<*>) = EqualByFields<T>(fields)
        }
    }

    private companion object {
        const val REPORT_NAME = "any-report-name"
        val REPORT_BOUND = ReportBound.DELTA_REPORT
    }
}
