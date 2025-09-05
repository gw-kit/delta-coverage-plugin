package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.data.instructions.ClassInstructions
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

// TODO move to file
internal data class ClassDataCopingContext(
    val className: String,
    val sourceProjectData: ProjectData,
    val copyToProjectData: ProjectData,
) {

    val sourceClassData: ClassData
        get() = sourceProjectData.getOrCreateClassData(className)

    private val allSourceClassInstructions: Map<String, ClassInstructions>
        get() = sourceProjectData.instructions

    private val copyToClassData: ClassData
        get() = copyToProjectData.getOrCreateClassData(className)

    private val copyToClassInstructions: ClassInstructions
        get() = copyToProjectData.instructions.computeIfAbsent(className) {
            ClassInstructions()
        }

    fun copyClassData(): ClassData {
        val sourceClass: ClassData = sourceClassData
        copyToClassData.apply {
            source = sourceClass.source
            merge(sourceClass)
        }

        copyClassInstructions()

        return copyToClassData
    }

    private fun copyClassInstructions() {
        val className: String = className
        allSourceClassInstructions[className]?.let { sourceClassInstructions ->
            copyToClassInstructions.merge(sourceClassInstructions)
        }
    }
}
