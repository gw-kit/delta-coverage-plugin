package io.github.gwkit.testimpact.gradle

import io.github.gwkit.testimpact.gradle.config.TestImpactConfiguration
import io.github.gwkit.testimpact.gradle.task.GenerateJfcConfigTask
import io.github.gwkit.testimpact.gradle.task.TestMappingAnalysisTask
import io.github.gwkit.testimpact.gradle.test.listener.TestEventsCollector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider
import java.io.File

open class TestImpactPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val config = project.extensions.create(
            EXTENSION_NAME,
            TestImpactConfiguration::class.java,
            project.objects,
        )

        val analyzeTask: TaskProvider<TestMappingAnalysisTask> = project.tasks.register(
            ANALYZE_TASK_NAME,
            TestMappingAnalysisTask::class.java
        ) { task ->
            task.onlyIf {
                config.enabled.get()
            }
            task.outputDirectory.set(
                project.layout.projectDirectory.dir(config.reportOutputDir)
            )
            task.includePackages.set(config.includePackages)
            task.excludePackages.set(config.excludePackages)
            task.htmlEnabled.set(config.reports.html)
            task.flamegraphEnabled.set(config.reports.flamegraph)
        }

        val generateJfcTask: TaskProvider<GenerateJfcConfigTask> = project.registerGenerateJfcTask(config)

        project.tasks.withType(Test::class.java).configureEach { testTask ->
            testTask.configureTestTask(config, generateJfcTask)
            with(analyzeTask.get()) {
                val jfrFile: File = testTask.temporaryDir.resolve(JFR_FILENAME)
                jfrFiles.from(jfrFile)
                testEventsFiles.from(testTask.temporaryDir.resolve(TEST_EVENTS_FILENAME))
                mustRunAfter(testTask)
            }
        }
    }

    private fun Project.registerGenerateJfcTask(
        config: TestImpactConfiguration,
    ): TaskProvider<GenerateJfcConfigTask> {
        val taskName = "generateJfcConfig"
        return project.tasks.register(taskName, GenerateJfcConfigTask::class.java) { task ->
            task.onlyIf { config.enabled.get() }
            task.jfcFile.set(layout.buildDirectory.file("sampling/stacktrace-sampling.jfc"))
        }
    }

    private fun Test.configureTestTask(
        config: TestImpactConfiguration,
        generateJfcTask: TaskProvider<GenerateJfcConfigTask>,
    ) {
        inputs.files(generateJfcTask)

        val jfrFile = temporaryDir.resolve(JFR_FILENAME)
        val testEventsFile = temporaryDir.resolve(TEST_EVENTS_FILENAME)

        jvmArgumentProviders.add(
            JfrCommandLineProvider(
                config.enabled,
                generateJfcTask.flatMap { it.jfcFile }.map { it.asFile },
                jfrFile
            )
        )

        addTestListener(TestEventsCollector(testEventsFile))
    }

    private class JfrCommandLineProvider(
        private val enabled: Provider<Boolean>,
        private val jfcFile: Provider<File>,
        private val jfrFile: File
    ) : CommandLineArgumentProvider {
        override fun asArguments(): Iterable<String> = if (enabled.get()) {
            val jvmArg = sequenceOf(
                "-XX:StartFlightRecording=filename=${jfrFile.absolutePath}",
                "settings=${jfcFile.get().absolutePath}",
                "dumponexit=true",
            ).joinToString(",")
            listOf(jvmArg)
        } else {
            emptyList()
        }
    }

    companion object {
        const val EXTENSION_NAME = "testImpact"
        const val ANALYZE_TASK_NAME = "analyzeTestMapping"

        private const val JFR_FILENAME = "recording.jfr"
        private const val TEST_EVENTS_FILENAME = "test-events.txt"
    }
}
