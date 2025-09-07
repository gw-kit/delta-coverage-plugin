package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import java.io.File

internal data class JvmClassDesc(
    val className: String,
    val file: File,
)
