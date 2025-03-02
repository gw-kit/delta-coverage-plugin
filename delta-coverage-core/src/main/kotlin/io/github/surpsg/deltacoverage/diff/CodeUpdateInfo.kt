@file:JvmName("CodeUpdateInfo")
package io.github.surpsg.deltacoverage.diff

import io.github.surpsg.deltacoverage.diff.parse.ClassFile

internal class CodeUpdateInfo(
    private val fileNameToModifiedLineNumbers: Map<String, Set<Int>>
) {

    fun getClassModifications(classFile: ClassFile): ClassModifications {
        return ClassModifications(
            getModInfoByClass(classFile)
        )
    }

    fun isInfoExists(classFile: ClassFile): Boolean {
        return getModInfoByClass(classFile).isNotEmpty()
    }

    private fun getModInfoByClass(classFile: ClassFile): Set<Int> {
        return fileNameToModifiedLineNumbers.asSequence()
            .filter { it.key.endsWith(classFile.path) }
            .map { it.value }
            .firstOrNull()
            .orEmpty()
    }
}

internal class ClassModifications(private val modifiedLines: Set<Int>) {
    fun isLineModified(lineNumber: Int): Boolean = modifiedLines.contains(lineNumber)
}
