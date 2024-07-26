package io.github.surpsg.deltacoverage.report.jacoco.csv

import io.github.surpsg.deltacoverage.report.textual.RawCoverageData
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import org.junit.jupiter.api.Test

class RawCoverageDataMapperTest {

    @Test
    fun `toCoverageData should map CsvCoverageView to RawCoverageData`() {
        // GIVEN
        val csvCoverageView = CsvCoverageView().apply {
            group = "group"
            aPackage = "aPackage"
            aClass = "aClass"
            branchCovered = "1"
            branchesMissed = "2"
            lineCovered = "3"
            lineMissed = "4"
            instrCovered = "5"
            instrMissed = "6"
        }

        // WHEN
        val rawCoverageData = csvCoverageView.toCoverageData()

        // THEN
        rawCoverageData shouldBeEqualToComparingFields RawCoverageData {
            aClass = "aPackage.aClass"
            branches(1, 3)
            lines(3, 7)
            instr(5, 11)
        }
    }
}
