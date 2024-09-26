package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.BranchData
import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.data.instructions.ClassInstructions
import com.intellij.rt.coverage.data.instructions.LineInstructions
import com.intellij.rt.coverage.report.XMLCoverageReport
import com.intellij.rt.coverage.util.ArrayUtil
import io.github.surpsg.deltacoverage.report.textual.RawCoverageData
import io.github.surpsg.deltacoverage.report.textual.RawCoverageDataProvider

internal class IntellijRawCoverageDataProvider(
    private val projectData: ProjectData,
) : RawCoverageDataProvider {

    override fun obtainData(): List<RawCoverageData> =
        XMLCoverageReport.mapClassesToPackages(projectData, true)
            .values.asSequence()
            .flatMap { it }
            .map { classData ->
                collectClassCoverageData(
                    classData,
                    projectData.instructions[classData.name]
                )
            }
            .toList()

    private fun collectClassCoverageData(
        classData: ClassData,
        classInstructions: ClassInstructions?,
    ): RawCoverageData {
        val classCoverage = RawCoverageData.newBlank {
            aClass = classData.name
        }
        val lineInstructions: Array<LineInstructions> = classInstructions?.getlines() ?: emptyArray()
        return classData.mapLinesToMethods().values
            .asSequence()
            .map { lineData: List<LineData> ->
                collectMethodData(lineData, lineInstructions)
            }
            .fold(classCoverage, RawCoverageData::merge)
    }

    private fun collectMethodData(
        lines: List<LineData>,
        allLineInstructions: Array<LineInstructions>,
    ): RawCoverageData =
        lines.asSequence()
            .map { lineData -> buildLineRawCoverage(lineData, allLineInstructions) }
            .reduce(RawCoverageData::merge)

    private fun buildLineRawCoverage(
        lineData: LineData,
        allLineInstructions: Array<LineInstructions>,
    ) = RawCoverageData.newBlank {
        lines(
            covered = if (lineData.hits > 0) 1 else 0,
            total = 1,
        )

        val branchData = lineData.branchData ?: BranchData(0, 0)
        branches(
            covered = branchData.coveredBranches,
            total = branchData.totalBranches,
        )

        val instructionsData = ArrayUtil.safeLoad(allLineInstructions, lineData.lineNumber)
            ?.getInstructionsData(lineData)
            ?: BranchData(0, 0)
        instr(
            covered = instructionsData.coveredBranches,
            total = instructionsData.totalBranches,
        )
    }
}
