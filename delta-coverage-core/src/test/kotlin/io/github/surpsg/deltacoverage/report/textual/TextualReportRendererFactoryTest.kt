package io.github.surpsg.deltacoverage.report.textual

import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.textual.asciitable.AsciiTableRenderer
import io.github.surpsg.deltacoverage.report.textual.markdown.MarkdownReportRenderer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TextualReportRendererFactoryTest {


    @ParameterizedTest
    @MethodSource("rendererFactoryTestParams")
    fun `should return correct renderer`(
        reportType: ReportType,
        expectedRenderer: TextualReportRenderer,
    ) {
        // WHEN
        val renderer = TextualReportRendererFactory.getBy(reportType)

        // THEN
        expectedRenderer shouldBe renderer
    }

    @Test
    fun `should throw if provided unsupported report type`() {
        assertThrows<IllegalStateException> {
            TextualReportRendererFactory.getBy(ReportType.XML)
        }
    }

    @Suppress("unused")
    private fun rendererFactoryTestParams() = ReportType.entries
        .mapNotNull {
            when (it) {
                ReportType.MARKDOWN -> it to MarkdownReportRenderer
                ReportType.CONSOLE -> it to AsciiTableRenderer

                ReportType.HTML, ReportType.XML -> null
            }
        }
        .map { arguments(it.first, it.second) }
}
