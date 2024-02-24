package io.github.surpsg.deltacoverage.report.console

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class ConsoleReportFacadeTest {

    @Test
    fun `generateReport should render report`() {
        val rawCoverageData = listOf(
            RawCoverageData.newBlank {
                group = "group1"
                aClass = "class1"
                branchesCovered = 1
                branchesTotal = 2
                linesCovered = 3
                linesTotal = 4
            },
            RawCoverageData.newBlank {
                group = "group2"
                aClass = "class2"
                branchesCovered = 5
                branchesTotal = 6
                linesCovered = 7
                linesTotal = 8
            }
        )
        val coverageDataProvider = object : RawCoverageDataProvider {
            override fun obtainData() = rawCoverageData
        }
        val outputStream = ByteArrayOutputStream()

        // WHEN
        ConsoleReportFacade.generateReport(
            coverageDataProvider,
            outputStream
        )

        // THEN
        val expectedReport = """
            +--------+--------+--------+----------+
            | Delta Coverage Stats                |
            +--------+--------+--------+----------+
            | Source | Class  | Lines  | Branches |
            +--------+--------+--------+----------+
            | group2 | class2 | 87.50% | 83.33%   |
            | group1 | class1 | 75%    | 50%      |
            +--------+--------+--------+----------+
            | Total  |        | 83.33% | 75%      |
            +--------+--------+--------+----------+
            
        """.trimIndent()
        String(outputStream.toByteArray()) shouldBe expectedReport
    }

    @Test
    fun `generateReport should render report with NA values`() {
        val rawCoverageData = listOf(
            RawCoverageData.newBlank {
                group = "group1"
                aClass = "class1"
                branchesCovered = 0
                branchesTotal = 0
                linesCovered = 0
                linesTotal = 0
            },
        )
        val coverageDataProvider = object : RawCoverageDataProvider {
            override fun obtainData() = rawCoverageData
        }
        val outputStream = ByteArrayOutputStream()

        // WHEN
        ConsoleReportFacade.generateReport(
            coverageDataProvider,
            outputStream
        )

        // THEN
        val expectedReport = """
            +--------+--------+-------+----------+
            | Delta Coverage Stats               |
            +--------+--------+-------+----------+
            | Source | Class  | Lines | Branches |
            +--------+--------+-------+----------+
            | group1 | class1 | NaN%  |          |
            +--------+--------+-------+----------+
            | Total  |        | NaN%  |          |
            +--------+--------+-------+----------+
            
        """.trimIndent()
        String(outputStream.toByteArray()) shouldBe expectedReport
    }
}
