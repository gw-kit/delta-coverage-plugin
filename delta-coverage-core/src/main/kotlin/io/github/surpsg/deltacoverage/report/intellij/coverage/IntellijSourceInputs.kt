package io.github.surpsg.deltacoverage.report.intellij.coverage

import java.io.File

internal data class IntellijSourceInputs(
    val allClasses: Set<File>,
    val classesRoots: List<File>,
    val excludeClasses: Set<String>,
    val sourcesFiles: List<File>,
)
