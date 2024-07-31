package io.github.surpsg.deltacoverage.report.textual

import java.io.OutputStream
import java.io.PrintWriter
import java.util.Collections
import kotlin.math.max

internal abstract class TextualReportRenderer {

    abstract val hDelim: String
    abstract val vDelim: String
    abstract val joinDelim: String

    fun render(context: Context) = with(context) {
        val printWriter = PrintWriter(outputStream, true)
        val widthMap: List<Int> = computeWidthMap()

        renderTitle(printWriter, widthMap)
        renderHeader(printWriter, widthMap)
        renderBody(printWriter, widthMap)
        renderFooter(printWriter, widthMap)
    }

    abstract fun Context.renderTitle(printWriter: PrintWriter, widthMap: List<Int>)

    abstract fun Context.renderHeader(printWriter: PrintWriter, widthMap: List<Int>)

    abstract fun Context.renderBody(printWriter: PrintWriter, widthMap: List<Int>)

    abstract fun Context.renderFooter(printWriter: PrintWriter, widthMap: List<Int>)

    private fun Context.computeWidthMap(): List<Int> {
        val widthMap: MutableList<Int> = MutableList(headers.size) { 0 }

        val allRows: List<List<String>> = mutableListOf(headers).apply {
            addAll(rows)
            addAll(multiLineFooter)
        }
        for (row in allRows) {
            for (i in row.indices) {
                val cellSize = row[i].length
                widthMap[i] = max(cellSize, widthMap[i])
            }
        }
        return widthMap
    }

    protected fun PrintWriter.printLine(widthMap: List<Int>) {
        for (i in widthMap.indices) {
            val line: String = Collections.nCopies(widthMap[i] + vDelim.length + 1, hDelim)
                .joinToString("") { it }
            print(joinDelim + line + (if (i == widthMap.size - 1) joinDelim else ""))
        }
        println()
    }

    @Suppress("ImplicitDefaultLocale")
    protected fun PrintWriter.printRow(cells: List<String>, widthMap: List<Int>) {
        for (i in cells.indices) {
            val verStrTemp = if (i == cells.size - 1) vDelim else ""

            print(
                String.format("%s %-${widthMap[i]}s %s", vDelim, cells[i], verStrTemp)
            )
        }
        println()
    }

    class Context private constructor(
        val outputStream: OutputStream,
        val title: String,
        val headers: List<String>,
        val rows: List<List<String>>,
        val multiLineFooter: List<List<String>>,
    ) {

        class Builder {

            lateinit var title: String
            lateinit var output: OutputStream
            lateinit var headers: List<String>
            lateinit var rows: List<List<String>>
            lateinit var footer: List<List<String>>

            fun build() = Context(output, title, headers, rows, footer)
        }

        companion object {
            operator fun invoke(block: Builder.() -> Unit): Context = Builder().apply(block).build()
        }
    }
}
