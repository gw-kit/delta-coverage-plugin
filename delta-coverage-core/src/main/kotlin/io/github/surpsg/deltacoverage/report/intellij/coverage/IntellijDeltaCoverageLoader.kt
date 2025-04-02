package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.FileMapData
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
        val projectDataCopy = ProjectData().apply {
            setInstructionsCoverage(true)
        }
        fullCoverage.copyAllClassDataWithFiltering(projectDataCopy, codeUpdateInfo)

        val mappings: Map<String, Array<FileMapData>> = fullCoverage.linesMap ?: emptyMap()
        mappings.forEach { (key, value) ->
            projectDataCopy.addLineMaps(key, value)
        }
        return projectDataCopy
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
        copyToProjectData: ProjectData,
        codeUpdateInfo: CodeUpdateInfo,
    ) {
        val sourceProjectData: ProjectData = this
        sourceProjectData.classesCollection.asSequence()
            .mapNotNull { sourceClassData ->
                val classFile: ClassFile = classFileFrom(sourceClassData)
                if (codeUpdateInfo.isInfoExists(classFile)) {
                    val classModifications = codeUpdateInfo.getClassModifications(classFile)
                    sourceClassData.filterLines(classModifications)
                } else {
                    null
                }
            }
            .map { sourceClassData ->
                ClassDataCopingContext(sourceClassData.name, sourceProjectData, copyToProjectData)
            }
            .forEach { classCopyContext ->
                classCopyContext.copyClassData()
            }
    }

    private data class ClassDataCopingContext(
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

        fun copyClassData() {
            val sourceClass: ClassData = sourceClassData
            copyToClassData.apply {
                source = sourceClass.source
                merge(sourceClass)
            }

            copyClassInstructions()
        }

        private fun copyClassInstructions() {
            val className: String = className
            allSourceClassInstructions[className]?.let { sourceClassInstructions ->
                copyToClassInstructions.merge(sourceClassInstructions, copyToClassData)
            }
        }
    }

    private fun classFileFrom(classData: ClassData) = ClassFile(
        sourceFileName = classData.source,
        className = classData.name
    )
}
