package io.github.gwkit.testimpact.gradle.sampling.testmapping.report

import groovy.json.JsonOutput
import java.io.File

/**
 * Generates a self-contained interactive HTML report from test mapping data.
 */
internal object HtmlTestMappingReporter : Reporter {

    private const val FILE_NAME = "test-mapping.html"
    private const val TEMPLATE_PATH = "/io/github/gwkit/testimpact/gradle/report/test-mapping-report.html"
    private const val DATA_PLACEHOLDER = "/*__DATA__*/null"

    override fun write(context: ReportContext): File {
        val jsonData = JsonOutput.toJson(context.report.toMap())
        val html = loadResource().replace(DATA_PLACEHOLDER, jsonData)

        return context.config.outputDir.resolve(FILE_NAME).apply {
            writeText(html)
        }
    }

    private fun loadResource(): String =
        HtmlTestMappingReporter::class.java.getResourceAsStream(TEMPLATE_PATH)
            ?.bufferedReader()
            ?.readText()
            ?: error("Template resource not found: $TEMPLATE_PATH")
}
