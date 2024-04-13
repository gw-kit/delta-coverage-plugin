package io.github.surpsg.deltacoverage.report.textual.markdown

import io.github.surpsg.deltacoverage.report.textual.TextualReportRenderer
import java.io.PrintWriter

internal object MarkdownReportRenderer : TextualReportRenderer() {

    override val hDelim: String = "-"
    override val vDelim: String = "|"
    override val joinDelim: String = "|"

    override fun Context.renderTitle(printWriter: PrintWriter, widthMap: List<Int>) = with(printWriter) {
        println("# $title")
        println()
    }

    override fun Context.renderHeader(printWriter: PrintWriter, widthMap: List<Int>) = with(printWriter) {
        printRow(headers, widthMap)
        printLine(widthMap)
    }

    override fun Context.renderBody(printWriter: PrintWriter, widthMap: List<Int>) = with(printWriter) {
        rows.forEach {
            printRow(it, widthMap)
        }
    }

    override fun Context.renderFooter(printWriter: PrintWriter, widthMap: List<Int>) =
        printWriter.printRow(footer, widthMap)
}
