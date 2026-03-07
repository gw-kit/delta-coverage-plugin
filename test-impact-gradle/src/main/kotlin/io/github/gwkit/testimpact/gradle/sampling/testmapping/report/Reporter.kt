package io.github.gwkit.testimpact.gradle.sampling.testmapping.report

import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.TestMappingReport
import java.io.File

/**
 * Input data available to all reporters.
 */
internal data class ReportContext(
    val config: ReportConfig,
    val report: TestMappingReport,
    val jfrFiles: Collection<File>,
    val testClasses: Set<String>,
)

/**
 * Generates a single report file.
 */
internal fun interface Reporter {
    fun write(context: ReportContext): File
}
