package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import java.nio.file.Path

internal data class JvmClassDesc(
    val className: String,
    val file: Path,
)
