package io.github.surpsg.deltacoverage.gradle

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

private const val TEST_KIT_FILE_PROP = "io.github.gwkit.coverjet.test-kit"

val GRADLE_HOME: String
    get() {
        val userHome: String = System.getProperty("user.home") ?: error("Cannot obtain 'user.home'.")
        return Path(userHome, ".gradle").absolutePathString()
    }

fun buildGradleRunner(
    projectRoot: File
): GradleRunner {
    return GradleRunner.create()
        .withPluginClasspath()
        .withProjectDir(projectRoot)
        .withTestKitDir(
            projectRoot.resolve(GRADLE_HOME).apply { mkdirs() }
        )
        .apply {
            val testKitPath: String = System.getProperty(TEST_KIT_FILE_PROP)
            File(projectDir, "gradle.properties").appendText(
                File(testKitPath).readText()
            )
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
    assertSoftly(output) {
        expectedString.forEach {
            shouldContain(it)
        }
    }
    return this
}
