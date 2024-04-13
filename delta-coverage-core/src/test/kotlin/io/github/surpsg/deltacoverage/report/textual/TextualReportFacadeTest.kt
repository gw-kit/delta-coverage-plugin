package io.github.surpsg.deltacoverage.report.textual

import io.github.surpsg.deltacoverage.report.ReportType
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class TextualReportFacadeTest {

    @Nested
    inner class ConsoleReportTest {

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
            val buildContext = TextualReportFacade.BuildContext(
                reportType = ReportType.CONSOLE,
                coverageDataProvider = coverageDataProvider,
                outputStream = outputStream,
            )

            // WHEN
            TextualReportFacade.generateReport(buildContext)

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
            val buildContext = TextualReportFacade.BuildContext(
                reportType = ReportType.CONSOLE,
                coverageDataProvider = coverageDataProvider,
                outputStream = outputStream,
            )

            // WHEN
            TextualReportFacade.generateReport(buildContext)

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

        @Test
        fun `generateReport should shrink class name to 100 symbols if exceeds 100 chars threshold`() {
            val seed = "t"
            val className = seed.repeat(123)
            val expectedClass = "..." + seed.repeat(97)
            val rawCoverageData = listOf(
                RawCoverageData.newBlank {
                    group = "any"
                    aClass = className
                    linesCovered = 1
                    linesTotal = 2
                },
            )
            val coverageDataProvider = object : RawCoverageDataProvider {
                override fun obtainData() = rawCoverageData
            }
            val outputStream = ByteArrayOutputStream()
            val buildContext = TextualReportFacade.BuildContext(
                reportType = ReportType.CONSOLE,
                coverageDataProvider = coverageDataProvider,
                outputStream = outputStream,
            )

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            outputStream.toString() shouldContain "| $expectedClass | 50%   |"
        }
    }

    @Nested
    inner class TextualReportTest {

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
            val buildContext = TextualReportFacade.BuildContext(
                reportType = ReportType.MARKDOWN,
                coverageDataProvider = coverageDataProvider,
                outputStream = outputStream,
            )

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            val expectedReport = """
            # Delta Coverage Stats
            
            | Source | Class  | Lines  | Branches |
            |--------|--------|--------|----------|
            | group2 | class2 | 87.50% | 83.33%   |
            | group1 | class1 | 75%    | 50%      |
            | Total  |        | 83.33% | 75%      |
            
        """.trimIndent()
            val actual = String(outputStream.toByteArray())
            actual shouldBe expectedReport
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
            val buildContext = TextualReportFacade.BuildContext(
                reportType = ReportType.MARKDOWN,
                coverageDataProvider = coverageDataProvider,
                outputStream = outputStream,
            )

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            val expectedReport = """
            # Delta Coverage Stats
            
            | Source | Class  | Lines | Branches |
            |--------|--------|-------|----------|
            | group1 | class1 | NaN%  |          |
            | Total  |        | NaN%  |          |
            
        """.trimIndent()
            val actual = String(outputStream.toByteArray())
            actual shouldBe expectedReport
        }

        @Test
        fun `generateReport should shrink class name to 100 symbols if exceeds 100 chars threshold`() {
            val seed = "t"
            val className = seed.repeat(123)
            val expectedClass = "..." + seed.repeat(97)
            val rawCoverageData = listOf(
                RawCoverageData.newBlank {
                    group = "any"
                    aClass = className
                    linesCovered = 1
                    linesTotal = 2
                },
            )
            val coverageDataProvider = object : RawCoverageDataProvider {
                override fun obtainData() = rawCoverageData
            }
            val outputStream = ByteArrayOutputStream()
            val buildContext = TextualReportFacade.BuildContext(
                reportType = ReportType.MARKDOWN,
                coverageDataProvider = coverageDataProvider,
                outputStream = outputStream,
            )

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            outputStream.toString() shouldContain "| $expectedClass | 50%   |"
        }
    }
}
