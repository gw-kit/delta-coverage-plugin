package io.github.gwkit.testimpact.gradle.sampling.testmapping.report

/**
 * Plain configuration for which report types to generate.
 */
import java.io.File

internal data class ReportConfig(
    val outputDir: File,
    val html: Boolean,
    val flamegraph: Boolean,
)
