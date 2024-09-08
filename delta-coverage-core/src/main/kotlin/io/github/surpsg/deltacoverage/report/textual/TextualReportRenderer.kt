package io.github.surpsg.deltacoverage.report.textual

import io.github.surpsg.deltacoverage.report.textual.BasicTextualReportRenderer.Context
import java.io.PrintWriter

internal interface TextualReportRenderer {

    fun render(context: Context)

    fun Context.renderTitle(printWriter: PrintWriter, widthMap: List<Int>)

    fun Context.renderHeader(printWriter: PrintWriter, widthMap: List<Int>)

    fun Context.renderBody(printWriter: PrintWriter, widthMap: List<Int>)

    fun Context.renderFooter(printWriter: PrintWriter, widthMap: List<Int>)
}
