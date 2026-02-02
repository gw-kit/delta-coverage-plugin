package io.github.surpsg.deltacoverage.gradle.test.sampling

import io.github.surpsg.deltacoverage.gradle.task.TestMappingAnalysisTask
import io.github.surpsg.deltacoverage.gradle.utils.deltaCoverageConfig
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Integrates test-to-code mapping with test tasks using JFR for stack trace collection.
 */
internal object TestMappingIntegration {

    private val log = LoggerFactory.getLogger(TestMappingIntegration::class.java)

    private const val JFR_FILENAME = "recording.jfr"
    private const val JFC_FILENAME = "stacktrace-sampling.jfc"
    private const val TEST_EVENTS_FILENAME = "test-events.txt"

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

    /**
     * Configures test tasks with JFR recording and test event collection if enabled.
     * For POC: only configures root project test tasks.
     */
    fun configure(project: Project) {
        val config = project.deltaCoverageConfig.testMapping

        // Register analysis task early
        val analyzeTask = project.tasks.register(
            "analyzeTestMapping",
            TestMappingAnalysisTask::class.java
        ) { task ->
            task.group = "verification"
            task.description = "Analyzes JFR recordings to map tests to code"
        }

        // Configure test tasks in root project only (POC simplification)
        // Using whenTaskAdded to catch all Test tasks, including those created by plugins
        project.tasks.withType(Test::class.java).all { testTask ->
            // Defer configuration to afterEvaluate to ensure DSL is processed
            project.afterEvaluate {
                if (config.enabled.get()) {
                    log.info("Configuring test task '${testTask.name}' for JFR recording")
                    configureTestTask(testTask)
                    analyzeTask.configure { task ->
                        task.jfrFiles.from(testTask.temporaryDir.resolve(JFR_FILENAME))
                        task.testEventsFiles.from(testTask.temporaryDir.resolve(TEST_EVENTS_FILENAME))
                        task.dependsOn(testTask)
                    }
                }
            }
        }
    }

    private fun configureTestTask(testTask: Test) {
        log.info("Configuring test task '${testTask.name}' for JFR recording")

        // 1. Create JFC config file in temp dir
        val jfcFile = testTask.temporaryDir.resolve(JFC_FILENAME)
        createJfcConfigFile(jfcFile)

        // 2. Add JFR JVM args with custom JFC settings
        val jfrFile = testTask.temporaryDir.resolve(JFR_FILENAME)
        testTask.jvmArgs(
            "-XX:StartFlightRecording=filename=${jfrFile.absolutePath},settings=${jfcFile.absolutePath},dumponexit=true"
        )

        // 3. Add TestListener for collecting test class names
        val testEventsFile = testTask.temporaryDir.resolve(TEST_EVENTS_FILENAME)
        testTask.addTestListener(TestEventsCollector(testEventsFile))

        // 4. Register outputs for up-to-date checks
        testTask.outputs.file(jfrFile)
        testTask.outputs.file(testEventsFile)
    }

    private fun createJfcConfigFile(jfcFile: File) {
        jfcFile.parentFile?.mkdirs()
        jfcFile.writeText(JFC_CONFIG)
        log.debug("Created JFC config file: {}", jfcFile.absolutePath)
    }
}
