package io.github.surpsg.deltacoverage.report.intellij.coverage

import java.io.File
import java.nio.file.Path

internal data class IntellijSourceInputs(
    val allClasses: Set<Path>,
    val classesRoots: List<File>,
    val excludeClasses: Set<String>,
    val sourcesFiles: List<File>,
)
