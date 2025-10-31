package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.diff.ClassModifications
import io.github.surpsg.deltacoverage.diff.CodeUpdateInfo
import io.github.surpsg.deltacoverage.diff.parse.ClassFile

@Suppress("UseOrEmpty")
internal object IntellijDeltaCoverageLoader {

    fun getDeltaProjectData(
        fullCoverage: ProjectData,
        codeUpdateInfo: CodeUpdateInfo
    ): ProjectData {
        return ProjectData().apply {
            setInstructionsCoverage(true)
            copyAllClassDataWithFiltering(fullCoverage, codeUpdateInfo)
        }
    }

    private fun ClassData.filterLines(classModifications: ClassModifications): ClassData {
        val lines: Array<LineData?> = classLines()
        for (i in lines.indices) {
            val actualLine: LineData = lines[i] ?: continue
            if (!classModifications.isLineModified(actualLine.lineNumber)) {
                lines[i] = null
            }
        }
        return this
    }

    private fun ClassData.classLines(): Array<LineData?> {
        return lines as? Array<LineData?>? ?: emptyArray()
    }

    private fun ProjectData.copyAllClassDataWithFiltering(
        sourceProjectData: ProjectData,
        codeUpdateInfo: CodeUpdateInfo,
    ) {
        val copyToProjectData: ProjectData = this
        sourceProjectData.classesCollection.asSequence()
            .filter { sourceClassData -> sourceClassData.source != null } // well some classes(lambda) without source
            .filter { sourceClassData ->
                val classFile: ClassFile = classFileFrom(sourceClassData)
                codeUpdateInfo.isInfoExists(classFile)
            }
            .map { sourceClassData ->
                ClassDataCopingContext(sourceClassData.name, sourceProjectData, copyToProjectData)
            }
            .forEach { classCopyContext ->
                val copied = classCopyContext.copyClassData()
                val classFile: ClassFile = classFileFrom(classCopyContext.sourceClassData)
                val classModifications = codeUpdateInfo.getClassModifications(classFile)
                copied.filterLines(classModifications)
            }
    }

    private fun classFileFrom(classData: ClassData) = ClassFile(
        sourceFileName = classData.source,
        className = classData.name
    )
}
