package io.github.surpsg.deltacoverage.report

import java.io.File

object ConsoleHtmlReportLinkRenderer {

    fun render(
        viewName: String,
        reportBound: ReportBound,
        reportPath: File,
    ) {
        println("[$viewName][$reportBound]: file://${reportPath.absolutePath}/index.html\n")
    }
}
