package io.github.surpsg.deltacoverage.report

import java.io.File

object ConsoleHtmlReportLinkRenderer {

    fun render(reportBound: ReportBound, reportPath: File) {
        println("[$reportBound]: file://${reportPath.absolutePath}/index.html")
    }
}
