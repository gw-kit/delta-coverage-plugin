package io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis

import jdk.jfr.consumer.RecordedFrame
import jdk.jfr.consumer.RecordingFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Reads JFR files and collects ALL execution sample stacks (unfiltered)
 * for full execution profile flamegraph rendering.
 */
internal object FlamegraphDataCollector {

    private val log: Logger = LoggerFactory.getLogger(FlamegraphDataCollector::class.java)

    fun collect(
        jfrFiles: Collection<File>,
        testClasses: Set<String> = emptySet(),
    ): FlamegraphData {
        val collapsedStacks: Map<String, Int> = jfrFiles
            .asSequence()
            .filter { it.exists() }
            .flatMap { jfrFile -> collectFromFile(jfrFile, testClasses) }
            .groupingBy { it }
            .eachCount()
        return FlamegraphData(collapsedStacks)
    }

    private fun collectFromFile(
        jfrFile: File,
        testClasses: Set<String>,
    ): Sequence<String> = try {
        log.info("Collecting flamegraph data from: {}", jfrFile.absolutePath)
        val frameFormatter: (RecordedFrame) -> String = frameFormatter(testClasses)
        RecordingFile.readAllEvents(jfrFile.toPath())
            .asSequence()
            .filter { it.eventType.name == "jdk.ExecutionSample" }
            .mapNotNull { it.stackTrace }
            .map { stackTrace -> trimToTestRoot(stackTrace.frames.reversed(), testClasses) }
            .map { trimmedFrames -> trimmedFrames.joinToString(";", transform = frameFormatter) }
            .filter { it.isNotEmpty() }
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        log.warn("Failed to read JFR file for flamegraph {}: {}", jfrFile.absolutePath, e.message)
        emptySequence()
    }

    private fun frameFormatter(
        testClasses: Set<String>
    ): (RecordedFrame) -> String = { frame ->
        val className = frame.method.type.name
        val name = "$className.${frame.method.name}"
        val isTestFrame = testClasses.any { className.contains(it) }
        val suffix: String = frame.frameSuffix(isTestFrame)
        "$name$suffix"
    }

    private fun RecordedFrame.frameSuffix(isTestFrame: Boolean): String = if (isTestFrame) {
        "_[k]"
    } else if (this.isJavaFrame) {
        when (this.type) {
            "JIT compiled" -> "_[j]"
            "Inlined" -> "_[i]"
            "Interpreted" -> "_[0]"
            else -> "_[j]"
        }
    } else {
        "_[c]"
    }

    /**
     * Trims the stack to start from the test frame, filtering out non-test stacks.
     *
     * Returns `null` if no test frame is found (stack is discarded).
     */
    private fun trimToTestRoot(
        frames: List<RecordedFrame>,
        testClasses: Set<String>,
    ): List<RecordedFrame> {
        if (testClasses.isEmpty()) return frames
        val testIndex = frames.indexOfFirst { frame ->
            testClasses.any { frame.method.type.name.contains(it) }
        }
        return if (testIndex >= 0) {
            frames.subList(testIndex, frames.size)
        } else emptyList()
    }

}
