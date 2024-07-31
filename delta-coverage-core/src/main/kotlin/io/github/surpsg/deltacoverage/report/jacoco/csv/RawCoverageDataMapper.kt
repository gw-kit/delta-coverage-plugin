package io.github.surpsg.deltacoverage.report.jacoco.csv

import io.github.surpsg.deltacoverage.report.textual.RawCoverageData

internal fun CsvCoverageView.toCoverageData(): RawCoverageData = let { csvView ->
    RawCoverageData {
        group = csvView.group
        aClass = "${aPackage}.${csvView.aClass}"

        csvView.branchCovered.toInt().let { covered ->
            branches(
                covered = covered,
                total = covered + csvView.branchesMissed.toInt(),
            )
        }

        csvView.lineCovered.toInt().let { covered ->
            lines(
                covered = covered,
                total = covered + csvView.lineMissed.toInt(),
            )
        }

        csvView.instrCovered.toInt().let { covered ->
            instr(
                covered = covered,
                total = covered + csvView.instrMissed.toInt(),
            )
        }
    }
}
