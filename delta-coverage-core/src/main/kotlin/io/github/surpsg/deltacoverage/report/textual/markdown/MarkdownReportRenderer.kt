package io.github.surpsg.deltacoverage.report.textual.markdown

import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.FAILURE_COV_CHAR
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.FAILURE_COV_ICON
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.SUCCESS_COV_CHAR
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.SUCCESS_COV_ICON
import io.github.surpsg.deltacoverage.report.textual.TextualReportRenderer
import java.io.PrintWriter

internal object MarkdownReportRenderer : TextualReportRenderer() {

    override val hDelim: String = "-"
    override val vDelim: String = "|"
    override val joinDelim: String = "|"

    override fun Context.renderTitle(printWriter: PrintWriter, widthMap: List<Int>) = Unit

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
        multiLineFooter.forEach { footer ->
            val decoratedFooter = footer.map {
                it.replace(SUCCESS_COV_CHAR, SUCCESS_COV_ICON).replace(FAILURE_COV_CHAR, FAILURE_COV_ICON)
            }
            printWriter.printRow(decoratedFooter, widthMap)
        }
}
