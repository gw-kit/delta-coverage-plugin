package io.github.surpsg.deltacoverage.report.intellij.coverage

import java.io.File

internal data class IntellijSourceInputs(
    val classesFiles: List<File>,
    val sourcesFiles: List<File>,
)
