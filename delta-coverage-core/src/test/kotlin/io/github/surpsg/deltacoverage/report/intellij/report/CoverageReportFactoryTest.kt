package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.Reporter
import com.intellij.rt.coverage.report.data.Filters
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.intellij.coverage.NamedReportLoadStrategy
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.equals.Equality
import io.kotest.equals.byReflectionUsingFields
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.sequences.shouldBeEmpty
import org.junit.jupiter.api.Test

class CoverageReportFactoryTest {

    @Test
    fun `reportBuildersBy should return empty iterable if no enabled reports`() {
        // GIVEN
        val config = ReportsConfig {
            html = ReportConfig { enabled = false }
            xml = ReportConfig { enabled = false }
            csv = ReportConfig { enabled = false }
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
        }
        val reportLoadStrategy = anyReportLoadStrategy()

        // WHEN
        val actualBuilders: Sequence<ReportBuilder> = CoverageReportFactory.reportBuildersBy(
            config,
            listOf(reportLoadStrategy)
        )

        // THEN
        assertSoftly(actualBuilders.toList()) {
            shouldHaveSize(2)

            shouldContain(
                HtmlReportBuilder(
                    REPORT_NAME,
                    config,
                    REPORT_BOUND,
                    Reporter(reportLoadStrategy.reportLoadStrategy)
                ),
                Equality.byReflectionUsingFields(
                    HtmlReportBuilder::reportName,
                    HtmlReportBuilder::reportBound,
                )
            )

            shouldContain(
                XmlReportBuilder(
                    config,
                    REPORT_BOUND,
                    Reporter(reportLoadStrategy.reportLoadStrategy)
                ),
                Equality.byReflectionUsingFields(HtmlReportBuilder::reportBound)
            )
        }
    }

    private fun anyReportLoadStrategy() = NamedReportLoadStrategy(
        reportName = REPORT_NAME,
        reportBound = REPORT_BOUND,
        reportLoadStrategy = ReportLoadStrategy.RawReportLoadStrategy(
            emptyList(),
            emptyList(),
            Filters.EMPTY
        ),
    )

    private companion object {
        const val REPORT_NAME = "any-report-name"
        val REPORT_BOUND = ReportBound.DELTA_REPORT
    }
}
