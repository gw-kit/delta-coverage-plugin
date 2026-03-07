package io.github.gwkit.testimpact.gradle.sampling.testmapping.report

import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.TestMappingReport
import java.io.File

/**
 * Orchestrates generation of all enabled report types.
 */
internal class ReportWriter(
    private val config: ReportConfig,
) {

    private val reporters: Iterable<Reporter> = buildReporters(config)

    fun write(report: TestMappingReport, jfrFiles: Collection<File>, testClasses: Set<String>): List<File> {
        config.outputDir.mkdirs()
        val context = ReportContext(config, report, jfrFiles, testClasses)
        return reporters.map { reporter -> reporter.write(context) }
    }

    companion object {

        private fun buildReporters(config: ReportConfig): Collection<Reporter> = mapOf(
            config.html to HtmlTestMappingReporter,
            config.flamegraph to AsyncProfilerFlamegraphReporter,
        )
            .filterKeys { it }
            .values
    }
}
