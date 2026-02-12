package io.github.gwkit.testimpact.gradle.sampling.testmapping.report

import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.HotMethod
import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.MethodMapping
import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.ReportSummary
import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.TestMappingReport

/**
 * Renders test mapping report to console with human-readable formatting.
 */
internal object ConsoleTestMappingReporter {

    private const val LINE_WIDTH = 80
    private const val TOP_METHODS_COUNT = 10
    private const val VISIBILITY_ABBREVIATION_LENGTH = 3

    fun render(report: TestMappingReport): String = buildString {
        appendLine()
        appendHeader("Test Mapping Report")
        appendLine()

        appendSummary(report.summary)
        appendLine()

        if (report.hotMethods.isNotEmpty()) {
            appendHotMethods(report.hotMethods)
            appendLine()
        }

        appendMethodDetails(report.mappings)
        appendLine()

        appendLine("─".repeat(LINE_WIDTH))
    }

    private fun StringBuilder.appendHeader(title: String) {
        appendLine("─".repeat(LINE_WIDTH))
        appendLine(" $title")
        appendLine("─".repeat(LINE_WIDTH))
    }

    private fun StringBuilder.appendSummary(summary: ReportSummary) {
        appendLine(" Summary")
        appendLine("   Methods:  ${summary.totalMethods}")
        appendLine("   Tests:    ${summary.totalTests}")
        appendLine("   Samples:  ${summary.totalSamples}")
    }

    private fun StringBuilder.appendHotMethods(hotMethods: List<HotMethod>) {
        appendLine(" Hot Methods (most sampled)")
        hotMethods.take(TOP_METHODS_COUNT).forEach { hot ->
            val method = formatMethodName(hot.method)
            val stats = "${hot.totalHits} hits, ${hot.testCount} tests"
            appendLine("   $method")
            appendLine("      $stats")
        }
    }

    private fun StringBuilder.appendMethodDetails(mappings: Map<String, Map<String, MethodMapping>>) {
        appendLine(" Method Coverage Details")

        mappings.entries
            .sortedBy { it.key }
            .forEach { (className, methods) ->
                appendLine()
                appendLine("   $className")

                methods.entries
                    .sortedByDescending { it.value.totalHits }
                    .forEach { (methodName, mapping) ->
                        appendMethodMapping(methodName, mapping)
                    }
            }
    }

    private fun StringBuilder.appendMethodMapping(methodName: String, mapping: MethodMapping) {
        val visibility = mapping.visibility.take(VISIBILITY_ABBREVIATION_LENGTH)
        val hitsInfo = "${mapping.totalHits} hits"
        val testsCount = "${mapping.tests.size} tests"

        appendLine("      [$visibility] $methodName  ($hitsInfo, $testsCount)")

        mapping.tests
            .sortedByDescending { it.samples }
            .forEach { test ->
                val testName = formatTestName(test.id)
                val depth = "d:${test.depth}"
                val samples = "${test.samples} samples"
                appendLine("         ├─ $testName ($depth, $samples)")
            }
    }

    private fun formatMethodName(fullMethod: String): String {
        val parts = fullMethod.split("#")
        if (parts.size != 2) return fullMethod

        val className = parts[0].substringAfterLast('.')
        val methodName = parts[1]
        return "$className#$methodName"
    }

    private fun formatTestName(testId: String): String {
        val parts = testId.split("#")
        if (parts.size != 2) return testId

        val className = parts[0].substringAfterLast('.')
        val methodName = parts[1]
        return "$className.$methodName"
    }
}
