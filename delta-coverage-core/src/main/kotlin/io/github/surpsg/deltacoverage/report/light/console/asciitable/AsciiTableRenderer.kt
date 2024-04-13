package io.github.surpsg.deltacoverage.report.light.console.asciitable

import io.github.surpsg.deltacoverage.report.light.LightReportRenderer
import java.io.PrintWriter
import java.util.Collections

internal object AsciiTableRenderer : LightReportRenderer() {

    private const val H_DELIM = "-"
    private const val V_DELIM: String = "|"
    private const val JOIN_DELIM: String = "+"

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
        printRow(footer, widthMap)
        printLine(widthMap)
    }

    @Suppress("ImplicitDefaultLocale")
    private fun PrintWriter.printTitle(title: String, widthMap: List<Int>) {
        val totalCellsLength = widthMap.sum()
        val leftRightPaddingLength = 2
        val totalLeftRightPadding = widthMap.size * 2 - leftRightPaddingLength
        val totalInterCellsDelim = widthMap.size - 1
        val normalizationLength = totalCellsLength + totalLeftRightPadding + totalInterCellsDelim
        print(
            String.format("%s %-${normalizationLength}s %s", V_DELIM, title, V_DELIM)
        )
        println()
    }

    private fun PrintWriter.printLine(widthMap: List<Int>) {
        for (i in widthMap.indices) {
            val line: String = Collections.nCopies(widthMap[i] + V_DELIM.length + 1, H_DELIM)
                .joinToString("") { it }
            print(JOIN_DELIM + line + (if (i == widthMap.size - 1) JOIN_DELIM else ""))
        }
        println()
    }

    @Suppress("ImplicitDefaultLocale")
    private fun PrintWriter.printRow(cells: List<String>, widthMap: List<Int>) {
        for (i in cells.indices) {
            val verStrTemp = if (i == cells.size - 1) V_DELIM else ""

            print(
                String.format("%s %-${widthMap[i]}s %s", V_DELIM, cells[i], verStrTemp)
            )
        }
        println()
    }
}
