package io.github.surpsg.deltacoverage.report.textual

import java.io.PrintWriter

internal object NoCoverageTextualReportRenderer : TextualReportRenderer {
    override fun render(context: BasicTextualReportRenderer.Context) {
        PrintWriter(context.outputStream, true).apply {
            println("[${context.title}]: No coverage data available ¯\\_(ツ)_/¯")
        }
    }

    override fun BasicTextualReportRenderer.Context.renderTitle(printWriter: PrintWriter, widthMap: List<Int>) = Unit

    override fun BasicTextualReportRenderer.Context.renderHeader(printWriter: PrintWriter, widthMap: List<Int>) = Unit

    override fun BasicTextualReportRenderer.Context.renderBody(printWriter: PrintWriter, widthMap: List<Int>) = Unit

    override fun BasicTextualReportRenderer.Context.renderFooter(printWriter: PrintWriter, widthMap: List<Int>) = Unit
}
