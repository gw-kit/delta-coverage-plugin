package io.github.surpsg.deltacoverage.report.textual.asciitable

import io.github.surpsg.deltacoverage.report.textual.TextualReportRenderer
import java.io.PrintWriter

internal object AsciiTableRenderer : TextualReportRenderer() {

    override val hDelim: String = "-"
    override val vDelim: String = "|"
    override val joinDelim: String = "+"

    override fun Context.renderTitle(printWriter: PrintWriter, widthMap: List<Int>) = with(printWriter) {
        printLine(widthMap)
        printTitle(title, widthMap)
        printLine(widthMap)
    }

    override fun Context.renderHeader(printWriter: PrintWriter, widthMap: List<Int>) = with(printWriter) {
        printRow(headers, widthMap)
        printLine(widthMap)
    }

    override fun Context.renderBody(printWriter: PrintWriter, widthMap: List<Int>) = with(printWriter) {
        rows.forEach {
            printRow(it, widthMap)
        }
        if (rows.isNotEmpty()) {
            printLine(widthMap)
        }
    }

    override fun Context.renderFooter(printWriter: PrintWriter, widthMap: List<Int>) = with(printWriter) {
        multiLineFooter.forEach { footer ->
            printRow(footer, widthMap)
            printLine(widthMap)
        }
    }

    @Suppress("ImplicitDefaultLocale")
    private fun PrintWriter.printTitle(title: String, widthMap: List<Int>) {
        val totalCellsLength = widthMap.sum()
        val leftRightPaddingLength = 2
        val totalLeftRightPadding = widthMap.size * 2 - leftRightPaddingLength
        val totalInterCellsDelim = widthMap.size - 1
        val normalizationLength = totalCellsLength + totalLeftRightPadding + totalInterCellsDelim
        print(
            String.format("%s %-${normalizationLength}s %s", vDelim, title, vDelim)
        )
        println()
    }
}
