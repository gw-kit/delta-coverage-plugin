package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.Reporter
import com.intellij.rt.coverage.report.api.Filters
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
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
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty

class CoverageReportFactoryTest {

    @Test
    fun `reportBuildersBy should return empty iterable if no enabled reports`() {
        // GIVEN
        val context = ReportContext(
            mockk<DiffSource>(),
            DeltaCoverageConfig {
                diffSourceConfig = DiffSourceConfig { diffBase = "any" }
                reportsConfig = ReportsConfig {
                    html = ReportConfig { enabled = false }
                    xml = ReportConfig { enabled = false }
                    csv = ReportConfig { enabled = false }
                    console = ReportConfig { enabled = false }
                    markdown = ReportConfig { enabled = false }
                }
            }
        )

        // WHEN
        val actualBuilders: Sequence<ReportBuilder> = CoverageReportFactory.reportBuildersBy(
            context,
            listOf(anyReportLoadStrategy())
        )

        // THEN
        actualBuilders.shouldBeEmpty()
    }

    @Test
    fun `reportBuildersBy should return empty iterable if no report loaders`() {
        // GIVEN
        val context = ReportContext(
            mockk<DiffSource>(),
            DeltaCoverageConfig {
                diffSourceConfig = DiffSourceConfig { diffBase = "any" }
                reportsConfig = ReportsConfig {
                    html = ReportConfig { enabled = true }
                    xml = ReportConfig { enabled = true }
                    csv = ReportConfig { enabled = true }
                    console = ReportConfig { enabled = true }
                    markdown = ReportConfig { enabled = true }
                }
            }
        )

        // WHEN
        val actualBuilders: Sequence<ReportBuilder> = CoverageReportFactory.reportBuildersBy(
            context,
            emptyList()
        )

        // THEN
        actualBuilders.shouldBeEmpty()
    }

    @Test
    fun `reportBuildersBy should throw if unsupported csv report is enabled`() {
        // GIVEN
        val context = ReportContext(
            mockk<DiffSource>(),
            DeltaCoverageConfig {
                diffSourceConfig = DiffSourceConfig { diffBase = "any" }
                reportsConfig = ReportsConfig {
                    csv = ReportConfig { enabled = true }
                }
            }
        )

        val reportLoadStrategies = listOf(anyReportLoadStrategy())

        // WHEN // THEN
        shouldThrow<IllegalStateException> {
            CoverageReportFactory.reportBuildersBy(context, reportLoadStrategies).toList()
        }
    }

    @Test
    fun `reportBuildersBy should return report builders`() {
        // GIVEN
        val context = ReportContext(
            mockk<DiffSource>(),
            DeltaCoverageConfig {
                diffSourceConfig = DiffSourceConfig { diffBase = "any" }
                reportsConfig = ReportsConfig {
                    html = ReportConfig { enabled = true }
                    xml = ReportConfig { enabled = true }
                    console = ReportConfig { enabled = true }
                    markdown = ReportConfig { enabled = true }
                }
            }
        )
        val reportLoadStrategy = anyReportLoadStrategy()

        // WHEN
        val actualBuilders: Sequence<ReportBuilder> = CoverageReportFactory.reportBuildersBy(
            context,
            listOf(reportLoadStrategy),
        )

        // THEN
        assertSoftly(actualBuilders.toList()) {
            shouldHaveSize(4)

            shouldContain(
                HtmlReportBuilder(
                    REPORT_NAME,
                    REPORT_BOUND,
                    context.deltaCoverageConfig.reportsConfig,
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
                    context.deltaCoverageConfig.reportsConfig,
                    Reporter(reportLoadStrategy.reportLoadStrategy)
                ),
                EqualByFields.fromFields(XmlReportBuilder::reportBound)
            )

            shouldContain(
                ConsoleReportBuilder(
                    context.deltaCoverageConfig.coverageRulesConfig,
                    REPORT_BOUND,
                    Reporter(reportLoadStrategy.reportLoadStrategy),
                ),
                EqualByFields.fromFields(ConsoleReportBuilder::reportBound)
            )

            shouldContain(
                MarkdownReportBuilder(
                    REPORT_BOUND,
                    context,
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
