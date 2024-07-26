package io.github.surpsg.deltacoverage.report.textual

import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class TextualReportFacadeTest {

    @Test
    fun `should throw if provided unsupported report type`() {
        shouldThrow<IllegalArgumentException> {
            TextualReportFacade.BuildContext {
                reportType = ReportType.XML
                reportBound = ReportBound.DELTA_REPORT
                coverageDataProvider = object : RawCoverageDataProvider {
                    override fun obtainData() = emptyList<RawCoverageData>()
                }
                outputStream = ByteArrayOutputStream()
            }
        }
    }

    @Nested
    inner class ConsoleReportTest {

        @Test
        fun `generateReport should render report`() {
            val rawCoverageData = listOf(
                RawCoverageData.newBlank {
                    aClass = "class1"
                    instr(2, 3)
                    branches(1, 2)
                    lines(3, 4)
                },
                RawCoverageData.newBlank {
                    aClass = "class2"
                    instr(1, 3)
                    branches(5, 6)
                    lines(7, 8)
                }
            )
            val stream = ByteArrayOutputStream()
            val buildContext = TextualReportFacade.BuildContext {
                reportType = ReportType.CONSOLE
                reportBound = ReportBound.DELTA_REPORT
                coverageDataProvider = object : RawCoverageDataProvider {
                    override fun obtainData() = rawCoverageData
                }
                outputStream = stream

                targetInstr(80)
                targetBranches(70)
                targetLines(90)
            }

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            val expectedReport = """
                +--------+----------+----------+--------+
                | Delta Coverage Stats                  |
                +--------+----------+----------+--------+
                | Class  | Lines    | Branches | Instr. |
                +--------+----------+----------+--------+
                | class2 | 87.50%   | 83.33%   | 33.33% |
                | class1 | 75%      | 50%      | 66.67% |
                +--------+----------+----------+--------+
                | Total  | ✖ 83.33% | ✔ 75%    | ✖ 50%  |
                |        | (90%)    | (70%)    | (80%)  |
                +--------+----------+----------+--------+
                
            """.trimIndent()
            stream.toString() shouldBe expectedReport
        }

        @Test
        fun `generateReport should render report with NA values`() {
            val rawCoverageData = listOf(
                RawCoverageData.newBlank {
                    aClass = "class1"
                    instr(0, 0)
                    branches(0, 0)
                    lines(0, 0)
                },
            )
            val stream = ByteArrayOutputStream()
            val buildContext = TextualReportFacade.BuildContext {
                reportType = ReportType.CONSOLE
                reportBound = ReportBound.DELTA_REPORT
                coverageDataProvider = object : RawCoverageDataProvider {
                    override fun obtainData() = rawCoverageData
                }
                outputStream = stream
                targetInstr(0)
                targetLines(0)
                targetBranches(0)
            }

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            val expectedReport = """
                +--------+-------+----------+--------+
                | Delta Coverage Stats               |
                +--------+-------+----------+--------+
                | Class  | Lines | Branches | Instr. |
                +--------+-------+----------+--------+
                | class1 | NaN%  |          | NaN%   |
                +--------+-------+----------+--------+
                | Total  | NaN%  |          | NaN%   |
                |        |       |          |        |
                +--------+-------+----------+--------+
                
            """.trimIndent()
            stream.toString() shouldBe expectedReport
        }

        @Test
        fun `generateReport should shrink class name if shrinking enabled and exceeds 100 chars threshold`() {
            val seed = "t"
            val className = seed.repeat(123)
            val expectedClass = "..." + seed.repeat(97)
            val rawCoverageData = listOf(
                RawCoverageData.newBlank {
                    aClass = className
                    lines(1, 2)
                },
            )
            val stream = ByteArrayOutputStream()
            val buildContext = TextualReportFacade.BuildContext {
                reportType = ReportType.CONSOLE
                reportBound = ReportBound.DELTA_REPORT
                coverageDataProvider = object : RawCoverageDataProvider {
                    override fun obtainData() = rawCoverageData
                }
                outputStream = stream
                shrinkLongClassName = true

                targetBranches(0)
                targetLines(0)
                targetInstr(0)
            }

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            stream.toString() shouldContain "| $expectedClass | 50%   |"
        }
    }

    @Nested
    inner class TextualReportTest {

        @Test
        fun `generateReport should render report`() {
            val rawCoverageData = listOf(
                RawCoverageData.newBlank {
                    aClass = "class1"
                    branches(1, 2)
                    lines(3, 4)
                    instr(5, 6)
                },
                RawCoverageData.newBlank {
                    aClass = "class2"
                    branches(5, 6)
                    lines(7, 8)
                    instr(9, 10)
                }
            )
            val stream = ByteArrayOutputStream()
            val buildContext = TextualReportFacade.BuildContext {
                reportType = ReportType.MARKDOWN
                reportBound = ReportBound.DELTA_REPORT
                coverageDataProvider = object : RawCoverageDataProvider {
                    override fun obtainData() = rawCoverageData
                }
                outputStream = stream
                targetBranches(75)
                targetLines(90)
                targetInstr(95)
            }

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            val expectedReport = """
            # Delta Coverage Stats
            
            | Class  | Lines    | Branches | Instr.   |
            |--------|----------|----------|----------|
            | class2 | 87.50%   | 83.33%   | 90%      |
            | class1 | 75%      | 50%      | 83.33%   |
            | Total  | 🔴 83.33% | 🟢 75%   | 🔴 87.50% |
            |        | (90%)    | (75%)    | (95%)    |
            
        """.trimIndent()
            stream.toString() shouldBe expectedReport
        }

        @Test
        fun `generateReport should render report with NA values`() {
            val rawCoverageData = listOf(
                RawCoverageData.newBlank {
                    aClass = "class1"
                },
            )
            val stream = ByteArrayOutputStream()
            val buildContext = TextualReportFacade.BuildContext {
                reportType = ReportType.MARKDOWN
                reportBound = ReportBound.DELTA_REPORT
                coverageDataProvider = object : RawCoverageDataProvider {
                    override fun obtainData() = rawCoverageData
                }
                outputStream = stream
                targetBranches(0)
                targetLines(0)
                targetInstr(0)
            }

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            val expectedReport = """
            # Delta Coverage Stats
            
            | Class  | Lines | Branches | Instr. |
            |--------|-------|----------|--------|
            | class1 | NaN%  |          | NaN%   |
            | Total  | NaN%  |          | NaN%   |
            |        |       |          |        |
            
        """.trimIndent()
            stream.toString() shouldBe expectedReport
        }

        @Test
        fun `generateReport should not shrink class name if shrinking is disabled`() {
            val seed = "t"
            val className = seed.repeat(123)
            val rawCoverageData = listOf(
                RawCoverageData.newBlank {
                    aClass = className
                    lines(1, 2)
                },
            )
            val stream = ByteArrayOutputStream()
            val buildContext = TextualReportFacade.BuildContext {
                reportType = ReportType.MARKDOWN
                reportBound = ReportBound.DELTA_REPORT
                coverageDataProvider = object : RawCoverageDataProvider {
                    override fun obtainData() = rawCoverageData
                }
                outputStream = stream
                shrinkLongClassName = false
                targetBranches(0)
                targetLines(0)
                targetInstr(0)
            }

            // WHEN
            TextualReportFacade.generateReport(buildContext)

            // THEN
            stream.toString() shouldContain "| $className | 50%   |"
        }
    }
}
