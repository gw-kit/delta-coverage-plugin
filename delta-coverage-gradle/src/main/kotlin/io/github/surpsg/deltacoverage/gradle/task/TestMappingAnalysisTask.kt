package io.github.surpsg.deltacoverage.gradle.task

import jdk.jfr.consumer.RecordedStackTrace
import jdk.jfr.consumer.RecordingFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * Task that analyzes JFR recordings and matches stack traces with test classes.
 */
abstract class TestMappingAnalysisTask : DefaultTask() {

    @get:InputFiles
    @get:Optional
    abstract val jfrFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:Optional
    abstract val testEventsFiles: ConfigurableFileCollection

    @TaskAction
    fun analyze() {
        val testClasses = loadTestClasses()
        if (testClasses.isEmpty()) {
            logger.lifecycle("No test classes found in test-events files")
            return
        }
        logger.lifecycle("Loaded ${testClasses.size} test classes:")
        testClasses.forEach {
            logger.lifecycle(it)
        }

        val matchedCount = analyzeJfrFiles(testClasses)
        logger.lifecycle("Matched stacktraces containing test classes: $matchedCount")
    }

    private fun loadTestClasses(): Set<String> {
        return testEventsFiles.files
            .filter { it.exists() }
            .flatMap { it.readLines() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun analyzeJfrFiles(testClasses: Set<String>): Int {
        var count = 0
        jfrFiles.files
            .filter { it.exists() }
            .forEach { jfrFile ->
                logger.info("Analyzing JFR file: ${jfrFile.absolutePath}")
                try {
                    RecordingFile.readAllEvents(jfrFile.toPath())
                        .filter { it.eventType.name == "jdk.ExecutionSample" }
                        .forEach { event ->
                            val stackTrace = event.stackTrace
                            if (stackTrace != null && containsTestClass(stackTrace, testClasses)) {
                                count++
                            }
                        }
                } catch (e: Exception) {
                    logger.warn("Failed to read JFR file ${jfrFile.absolutePath}: ${e.message}")
                }
            }
        return count
    }

    private fun containsTestClass(stackTrace: RecordedStackTrace, testClasses: Set<String>): Boolean {
        return stackTrace.frames.any { frame ->
            val className = frame.method.type.name
            testClasses.any { testClass -> className.contains(testClass) }
        }
    }
}
