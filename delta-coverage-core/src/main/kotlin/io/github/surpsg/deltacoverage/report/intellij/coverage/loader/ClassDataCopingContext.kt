package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.data.instructions.ClassInstructions

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
