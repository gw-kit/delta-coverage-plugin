package io.github.surpsg.deltacoverage.report.console.asciitable

import java.io.OutputStream
import java.io.PrintWriter
import java.util.Collections
import kotlin.math.max

internal object AsciiTableRenderer {

    private const val H_DELIM = "-"
    private const val V_DELIM: String = "|"
    private const val JOIN_DELIM: String = "+"

    fun render(context: Context) = with(context) {
        val printWriter = PrintWriter(outputStream, true)
        val widthMap: List<Int> = computeWidthMap()

        with(printWriter) {
            printLine(widthMap)

            printTitle(title, widthMap)
            printLine(widthMap)

            printRow(headers, widthMap)
            printLine(widthMap)

            printMainContent(rows, widthMap)

            printRow(footer, widthMap)
            printLine(widthMap)
        }
    }

    private fun Context.computeWidthMap(): List<Int> {
        val widthMap: MutableList<Int> = MutableList(headers.size) { 0 }

        val allRows: List<List<String>> = mutableListOf(headers, footer).apply { addAll(rows) }
        for (row in allRows) {
            for (i in row.indices) {
                val cellSize = row[i].length
                widthMap[i] = max(cellSize, widthMap[i])
            }
        }
        return widthMap
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

    private fun PrintWriter.printMainContent(rows: List<List<String>>,widthMap: List<Int>) {
        rows.forEach {
            printRow(it, widthMap)
        }
        if (rows.isNotEmpty()) {
            printLine(widthMap)
        }
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

    class Context private constructor(
        val outputStream: OutputStream,
        val title: String,
        val headers: List<String>,
        val rows: List<List<String>>,
        val footer: List<String>,
    ) {

        class Builder {

            lateinit var title: String
            lateinit var output: OutputStream
            lateinit var headers: List<String>
            lateinit var rows: List<List<String>>
            lateinit var footer: List<String>

            fun build() = Context(output, title, headers, rows, footer)
        }

        companion object {
            operator fun invoke(block: Builder.() -> Unit): Context = Builder().apply(block).build()
        }
    }
}
