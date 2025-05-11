package io.github.surpsg.deltacoverage.gradle.test.assertion

import io.github.surpsg.deltacoverage.report.ReportBound
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.file.shouldExist
import java.io.File

fun File.assertSummaryReportExist(reportBound: ReportBound, vararg views: String) = assertSoftly(this) {
    shouldExist()

    views.forEach { view ->
        val prefix = if (reportBound == ReportBound.FULL_REPORT) "full-coverage-$view" else view
        shouldContainFile("$prefix-summary.json")
    }
}
