package io.github.surpsg.deltacoverage.gradle.task

import jdk.jfr.consumer.RecordedEvent
import jdk.jfr.consumer.RecordedFrame
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

        analyzeJfrFiles(testClasses)
    }

    private fun loadTestClasses(): Set<String> {
        return testEventsFiles.files
            .filter { it.exists() }
            .flatMap { it.readLines() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun analyzeJfrFiles(testClasses: Set<String>) {
        jfrFiles.files
            .filter { it.exists() }
            .forEach { jfrFile ->
                logger.info("Analyzing JFR file: ${jfrFile.absolutePath}")
                val associations: Map<String, Set<String>> = try {
                    RecordingFile.readAllEvents(jfrFile.toPath())
                        .asSequence()
                        .filter { it.eventType.name == "jdk.ExecutionSample" }
                        .flatMap { event: RecordedEvent ->
                            resolveTestToMethodMapping(testClasses, event.stackTrace).entries.asSequence()
                        }
                        .fold(mutableMapOf()) { aggMap, entry ->
                            aggMap.merge(entry.key, entry.value) { prev, current ->
                                prev + current
                            }
                            aggMap
                        }
                } catch (e: Exception) {
                    logger.warn("Failed to read JFR file ${jfrFile.absolutePath}: ${e.message}")
                    emptyMap()
                }

                associations.forEach { (test, classes) ->
                    println("Test: ${test} (number=${classes.size})")
                    classes.forEach {
                        println("\t$it")
                    }
                    println("===============")
                }
            }
    }

    private fun resolveTestToMethodMapping(
        testClasses: Set<String>,
        stackTrace: RecordedStackTrace,
    ): Map<String, Set<String>> {
        val frames: Sequence<RecordedFrame> = stackTrace.frames.reversed().asSequence()
        return testClasses
            .associateWith { testClass ->
                frames
                    .dropWhile { frame -> !frame.method.type.name.contains(testClass) }
                    .filter { frame -> !frame.method.type.name.contains(testClass) }
                    .take(MAX_CALL_DEPS)
                    .mapIndexed { index, frame -> "[Depth=${index + 1}] ${frame.method.type.name}#${frame.method.name}:${frame.lineNumber}" }
                    .toSet()
            }
            .filterValues { it.isNotEmpty() }
    }

    private companion object {
        private const val MAX_CALL_DEPS = 2
    }
}
