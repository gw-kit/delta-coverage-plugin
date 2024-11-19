package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_TASK
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTaskConfigurer
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

const val TEST_VIEW = TEST_TASK_NAME
const val AGGREGATED_VIEW = DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME

fun String.deltaCoverageTask(): TaskName = TaskName(
    name = ":$DELTA_COVERAGE_TASK${this.replaceFirstChar { it.uppercase() }}",
)

fun GradleRunner.runDeltaCoverageTask(printLogs: Boolean = false, vararg gradleArgs: String): BuildResult {
    val composedGradleArgs: Array<String> = composeGradleArgs(*gradleArgs)
    return runTask(*composedGradleArgs)
        .apply { printLogs(printLogs) }
        .assertTaskStatusEqualsTo(
            TaskName(":$DELTA_COVERAGE_TASK"),
            TaskOutcome.SUCCESS
        )
}

fun GradleRunner.runDeltaCoverageTaskAndFail(printLogs: Boolean = false, vararg gradleArgs: String): BuildResult {
    val composedGradleArgs: Array<String> = composeGradleArgs(*gradleArgs)
    return runTaskAndFail(*composedGradleArgs)
        .apply { printLogs(printLogs) }
}

private fun composeGradleArgs(vararg gradleArgs: String): Array<String> {
    return arrayOf(TEST_TASK_NAME, DELTA_COVERAGE_TASK) + gradleArgs
}

private fun BuildResult.printLogs(enabled: Boolean) {
    if (enabled) {
        println(
            """
            =================== <Build logs> ===================
            $output
            =================== </Build logs> ==================
        """.trimIndent()
        )
    }
}

fun BuildResult.assertTaskStatusEqualsTo(taskName: TaskName, status: TaskOutcome): BuildResult {
    assertSoftly(output) {
        assertSoftly(taskName) { taskName ->
            task(taskName.toString())
                .shouldNotBeNull()
                .outcome.shouldBeEqualComparingTo(status)
        }
    }
    return this
}

fun expectedHtmlReportFiles(vararg packages: String): Array<String> = arrayOf(
    "index.html",
    "jacoco-resources",
    "jacoco-sessions.html"
) + packages

class TaskName(
    private val name: String,
) {
    override fun toString(): String = name
}
