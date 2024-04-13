package io.github.surpsg.deltacoverage.report.light

import java.io.OutputStream
import java.io.PrintWriter
import kotlin.math.max

internal abstract class LightReportRenderer {

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

        val allRows: List<List<String>> = mutableListOf(headers, footer).apply { addAll(rows) }
        for (row in allRows) {
            for (i in row.indices) {
                val cellSize = row[i].length
                widthMap[i] = max(cellSize, widthMap[i])
            }
        }
        return widthMap
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
