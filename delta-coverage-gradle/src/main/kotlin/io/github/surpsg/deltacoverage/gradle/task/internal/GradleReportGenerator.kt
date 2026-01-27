package io.github.surpsg.deltacoverage.gradle.task.internal

internal interface GradleReportGenerator {
    fun generateReport()

    companion object {
        val NOOP: GradleReportGenerator = object : GradleReportGenerator {
            override fun generateReport() = Unit
        }
    }
}
