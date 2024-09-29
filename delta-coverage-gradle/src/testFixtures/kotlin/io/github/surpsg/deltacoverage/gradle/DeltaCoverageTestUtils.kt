package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_TASK
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

fun GradleRunner.runDeltaCoverageTask(printLogs: Boolean = false, vararg gradleArgs: String): BuildResult {
    val composedGradleArgs: Array<String> = composeGradleArgs(*gradleArgs)
    return runTask(*composedGradleArgs)
        .apply { printLogs(printLogs) }
        .assertDeltaCoverageStatusEqualsTo(TaskOutcome.SUCCESS)
}

fun GradleRunner.runDeltaCoverageTaskAndFail(printLogs: Boolean = false, vararg gradleArgs: String): BuildResult {
    val composedGradleArgs: Array<String> = composeGradleArgs(*gradleArgs)
    return runTaskAndFail(*composedGradleArgs)
        .apply { printLogs(printLogs) }
        .assertDeltaCoverageStatusEqualsTo(TaskOutcome.FAILED)
}

private fun composeGradleArgs(vararg gradleArgs: String): Array<String> {
    return arrayOf(TEST_TASK_NAME, DELTA_COVERAGE_TASK) + gradleArgs
}

private fun BuildResult.printLogs(enabled: Boolean) {
    if (enabled) {
        println("""
            =================== <Build logs> ===================
            $output
            =================== </Build logs> ==================
        """.trimIndent())
    }
}

fun BuildResult.assertDeltaCoverageStatusEqualsTo(status: TaskOutcome): BuildResult {
    assertSoftly(output) {
        task(":$DELTA_COVERAGE_TASK")
            .shouldNotBeNull()
            .outcome shouldBeEqualComparingTo status
    }
    return this
}

fun expectedHtmlReportFiles(vararg packages: String): Array<String> = arrayOf(
    "index.html",
    "jacoco-resources",
    "jacoco-sessions.html"
) + packages
