package io.github.gwkit.testimpact.gradle

import io.github.gwkit.testimpact.gradle.config.TestImpactConfiguration
import io.github.gwkit.testimpact.gradle.task.TestMappingAnalysisTask
import io.github.gwkit.testimpact.gradle.test.listener.TestEventsCollector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider
import org.slf4j.LoggerFactory
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
            task.outputFile.set(
                project.layout.projectDirectory.file(config.reportOutputLocation)
            )
            task.includePackages.set(config.includePackages)
            task.excludePackages.set(config.excludePackages)
        }
        project.tasks.withType(Test::class.java).configureEach { testTask ->
            configureTestTask(testTask, config)
            with(analyzeTask.get()) {
                val jfrFile: File = testTask.temporaryDir.resolve(JFR_FILENAME)
                jfrFiles.from(jfrFile)
                testEventsFiles.from(testTask.temporaryDir.resolve(TEST_EVENTS_FILENAME))
                mustRunAfter(testTask)
            }
        }
    }

    private fun configureTestTask(testTask: Test, config: TestImpactConfiguration) {
        val jfcFile = testTask.temporaryDir.resolve(JFC_FILENAME).apply {
            createJfcConfigFile(this)
        }
        val jfrFile = testTask.temporaryDir.resolve(JFR_FILENAME)
        val testEventsFile = testTask.temporaryDir.resolve(TEST_EVENTS_FILENAME)

        testTask.jvmArgumentProviders.add(
            JfrCommandLineProvider(
                config.enabled,
                jfcFile,
                jfrFile
            )
        )

        testTask.addTestListener(TestEventsCollector(testEventsFile))
    }

    private class JfrCommandLineProvider(
        private val enabled: Provider<Boolean>,
        private val jfcFile: File,
        private val jfrFile: File
    ) : CommandLineArgumentProvider {
        override fun asArguments(): Iterable<String> = if (enabled.get()) {
            val jvmArg = sequenceOf(
                "-XX:StartFlightRecording=filename=${jfrFile.absolutePath}",
                "settings=${jfcFile.absolutePath}",
                "dumponexit=true",
            ).joinToString(",")
            listOf(jvmArg)
        } else {
            emptyList()
        }
    }

    private fun createJfcConfigFile(jfcFile: File) {
        jfcFile.parentFile?.mkdirs()
        jfcFile.writeText(JFC_CONFIG)
        log.debug("Created JFC config file: {}", jfcFile.absolutePath)
    }

    companion object {
        const val EXTENSION_NAME = "testImpact"
        const val ANALYZE_TASK_NAME = "analyzeTestMapping"

        private const val JFR_FILENAME = "recording.jfr"
        private const val JFC_FILENAME = "stacktrace-sampling.jfc"
        private const val TEST_EVENTS_FILENAME = "test-events.txt"

        private val log = LoggerFactory.getLogger(TestImpactPlugin::class.java)

        /**
         * JFC configuration optimized for stack trace sampling.
         * - ExecutionSample at 1ms interval for frequent sampling
         * - All other events disabled to minimize overhead
         */
        private val JFC_CONFIG = """
            <?xml version="1.0" encoding="UTF-8"?>
            <configuration version="2.0">
                <event name="jdk.ExecutionSample">
                    <setting name="enabled">true</setting>
                    <setting name="period">1 ms</setting>
                    <setting name="stackTrace">true</setting>
                </event>
                <event name="jdk.NativeMethodSample">
                    <setting name="enabled">false</setting>
                </event>
                <event name="jdk.ActiveRecording">
                    <setting name="enabled">false</setting>
                </event>
                <event name="jdk.ActiveSetting">
                    <setting name="enabled">false</setting>
                </event>
            </configuration>
        """.trimIndent()
    }
}
