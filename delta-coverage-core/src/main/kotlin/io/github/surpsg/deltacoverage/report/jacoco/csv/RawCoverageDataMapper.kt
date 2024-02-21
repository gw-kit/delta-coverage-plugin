package io.github.surpsg.deltacoverage.report.jacoco.csv

import io.github.surpsg.deltacoverage.report.console.RawCoverageData

internal fun CsvCoverageView.toCoverageData(): RawCoverageData = let { csvView ->
    RawCoverageData {
        group = csvView.group
        aClass = "${aPackage}.${csvView.aClass}"

        csvView.branchCovered.toInt().let { covered ->
            branchesCovered = covered
            branchesTotal = covered + csvView.branchesMissed.toInt()
        }

        csvView.lineCovered.toInt().let { covered ->
            linesCovered = covered
            linesTotal = covered + csvView.lineMissed.toInt()
        }
    }
}
