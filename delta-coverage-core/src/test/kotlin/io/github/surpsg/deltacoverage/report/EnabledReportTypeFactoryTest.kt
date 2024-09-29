package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KMutableProperty1

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnabledReportTypeFactoryTest {

    @Test
    fun `should return all reports when all reports enabled`() {
        // GIVEN
        val config = ReportsConfig {
            html = ReportConfig { enabled = true }
            xml = ReportConfig { enabled = true }
            console = ReportConfig { enabled = true }
            markdown = ReportConfig { enabled = true }
        }

        // WHEN
        val enabledReports = EnabledReportTypeFactory.obtain(config)

        // THEN
        enabledReports shouldContainExactlyInAnyOrder ReportType.entries
    }

    @Test
    fun `should return empty collection when all disabled`() {
        // GIVEN
        val config = ReportsConfig {
            html = ReportConfig { enabled = false }
            xml = ReportConfig { enabled = false }
            console = ReportConfig { enabled = false }
            markdown = ReportConfig { enabled = false }
        }

        // WHEN
        val enabledReports = EnabledReportTypeFactory.obtain(config)

        // THEN
        enabledReports.shouldBeEmpty()
    }

    @ParameterizedTest
    @MethodSource("enabledReportTestParams")
    fun `should return list only with enabled report`(
        expectedReportType: ReportType,
        enabledReportProp: KMutableProperty1<ReportsConfig.Builder, ReportConfig>,
    ) {
        // GIVEN
        val config = ReportsConfig {
            enabledReportProp.set(
                this,
                ReportConfig { enabled = true }
            )
        }

        // WHEN
        val enabledReports = EnabledReportTypeFactory.obtain(config)

        // THEN
        
        enabledReports.shouldHaveSize(1)
        enabledReports.shouldContain(expectedReportType)
    }

    @Suppress("unused")
    private fun enabledReportTestParams(): List<Arguments> = ReportType.entries.asSequence()
        .map {
            when(it) {
                ReportType.CONSOLE -> it to ReportsConfig.Builder::console
                ReportType.HTML -> it to ReportsConfig.Builder::html
                ReportType.MARKDOWN -> it to ReportsConfig.Builder::markdown
                ReportType.XML -> it to ReportsConfig.Builder::xml
            }
        }
        .map { arguments(it.first, it.second) }
        .toList()
}
