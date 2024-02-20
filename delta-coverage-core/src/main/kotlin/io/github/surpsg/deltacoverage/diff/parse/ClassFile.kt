package io.github.surpsg.deltacoverage.diff.parse

import java.nio.file.Path
import java.nio.file.Paths

class ClassFile(
    private val sourceFileName: String,
    private val className: String
) {
    val path: String by lazy {
        computePath()
    }

    private fun computePath(): String {
        val pathFromClass: Path = Paths.get(className)
        return if (pathFromClass.parent != null) {
            pathFromClass.parent.resolveWithNormalize(sourceFileName)
        } else {
            guessPathFromPackage()
        }
    }

    private fun guessPathFromPackage(): String {
        val filePathSuffix = className.replace(".", "/")
        return Paths.get(filePathSuffix).parent?.resolveWithNormalize(sourceFileName)
            ?: sourceFileNameWithSlash()
    }

    private fun sourceFileNameWithSlash(): String {
        return "/$sourceFileName"
    }

    private fun Path.resolveWithNormalize(fileName: String): String {
        return resolve(fileName)
            .toString()
            .replace("\\", "/")
    }
}
