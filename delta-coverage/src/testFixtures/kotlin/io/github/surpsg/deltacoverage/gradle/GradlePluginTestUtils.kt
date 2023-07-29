package io.github.surpsg.deltacoverage.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun buildGradleRunner(
    projectRoot: File
): GradleRunner {
    return GradleRunner.create()
        .withPluginClasspath()
        .withProjectDir(projectRoot)
        .withTestKitDir(projectRoot.resolve("TestKitDir").apply {
            mkdir()
        })
        .apply {
            // gradle testkit jacoco support
            javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")?.use { inputStream ->
                File(projectDir, "gradle.properties").outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
}

fun GradleRunner.runTask(vararg task: String): BuildResult {
    return tasksWithDebugOption(*task).build()
}

fun GradleRunner.runTaskAndFail(vararg task: String): BuildResult {
    return tasksWithDebugOption(*task).buildAndFail()
}

private fun GradleRunner.tasksWithDebugOption(vararg task: String): GradleRunner {
    val arguments: List<String> = mutableListOf(*task) + "-si"
    return withArguments(*arguments.toTypedArray())
}

fun BuildResult.assertOutputContainsStrings(vararg expectedString: String): BuildResult {
    assertThat(output).contains(*expectedString)
    return this
}
