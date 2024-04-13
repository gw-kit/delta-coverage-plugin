package io.github.surpsg.deltacoverage.report.textual

import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.textual.asciitable.AsciiTableRenderer
import io.github.surpsg.deltacoverage.report.textual.markdown.MarkdownReportRenderer

internal object TextualReportRendererFactory {

    fun getBy(reportType: ReportType): TextualReportRenderer = when (reportType) {
        ReportType.MARKDOWN -> MarkdownReportRenderer
        ReportType.CONSOLE -> AsciiTableRenderer
        else -> error("Unsupported report type for textual rendering: $reportType")
    }
}
