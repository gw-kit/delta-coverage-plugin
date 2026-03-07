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
        val config: TestImpactConfiguration = project.extensions.create(
            EXTENSION_NAME,
            TestImpactConfiguration::class.java,
            project.objects,
        )

        project.configureAllTestTasks(config)
    }

    private fun Project.configureAllTestTasks(
        config: TestImpactConfiguration,
    ) {
        val generateJfcTask: TaskProvider<GenerateJfcConfigTask> = project.registerGenerateJfcTask(config)
        project.allprojects { proj ->
            val analyzeTask: TaskProvider<TestMappingAnalysisTask> = proj.registerAnalyzeTask(config)
            proj.tasks.withType(Test::class.java).configureEach { testTask ->
                val jfrOutputs: TestImpactOutputs = testTask.applyStackTracesCollecting(config, generateJfcTask)
                with(analyzeTask.get()) {
                    jfrFiles.from(jfrOutputs.jfrData)
                    testEventsFiles.from(jfrOutputs.testEventsFile)
                    mustRunAfter(testTask)
                }
            }
        }
    }

    private fun Project.registerAnalyzeTask(
        config: TestImpactConfiguration,
    ): TaskProvider<TestMappingAnalysisTask> = tasks.register(
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

    private fun Project.registerGenerateJfcTask(
        config: TestImpactConfiguration,
    ): TaskProvider<GenerateJfcConfigTask> {
        val taskName = "generateJfcConfig"
        return project.tasks.register(taskName, GenerateJfcConfigTask::class.java) { task ->
            task.onlyIf { config.enabled.get() }
            task.jfcFile.set(layout.buildDirectory.file("sampling/stacktrace-sampling.jfc"))
        }
    }

    private fun Test.applyStackTracesCollecting(
        config: TestImpactConfiguration,
        generateJfcTask: TaskProvider<GenerateJfcConfigTask>,
    ): TestImpactOutputs {
        val additionalTaskOutputs = TestImpactOutputs(
            testEventsFile = temporaryDir.resolve("${name}-$TEST_EVENTS_FILENAME"),
            jfrData = temporaryDir.resolve("${name}-$JFR_FILENAME"),
        )
        inputs.files(generateJfcTask)
        outputs.files(
            additionalTaskOutputs.jfrData,
            additionalTaskOutputs.testEventsFile,
        )

        if (config.enabled.get()) {
            addTestListener(TestEventsCollector(additionalTaskOutputs.testEventsFile))

            jvmArgumentProviders.add(
                JfrCommandLineProvider(
                    config.enabled,
                    generateJfcTask.flatMap { it.jfcFile }.map { it.asFile },
                    additionalTaskOutputs.jfrData
                )
            )
        }

        return additionalTaskOutputs
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

    private data class TestImpactOutputs(
        val testEventsFile: File,
        val jfrData: File,
    )

    companion object {
        const val EXTENSION_NAME = "testImpact"
        const val ANALYZE_TASK_NAME = "analyzeTestMapping"

        private const val JFR_FILENAME = "recording.jfr"
        private const val TEST_EVENTS_FILENAME = "test-events.txt"
    }
}
