package io.github.surpsg.deltacoverage.gradle.test.sampling

import jdk.jfr.consumer.RecordedStackTrace
import jdk.jfr.consumer.RecordingFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant

/**
 * Analyzes JFR recordings to build test-to-code mappings.
 */
internal class JfrTestMappingAnalyzer(
    private val config: AnalyzerConfig = AnalyzerConfig()
) {
    private val log: Logger = LoggerFactory.getLogger(JfrTestMappingAnalyzer::class.java)

    fun analyze(jfrFiles: Collection<File>, testClasses: Set<String>): TestMappingReport {
        if (testClasses.isEmpty()) {
            return buildReport(MappingResult(emptyMap(), 0), emptySet())
        }

        val result = analyzeJfrFiles(jfrFiles, testClasses)
        return buildReport(result, testClasses)
    }

    private fun analyzeJfrFiles(jfrFiles: Collection<File>, testClasses: Set<String>): MappingResult {
        val methodMappings = mutableMapOf<MethodKey, MutableMap<String, TestHit>>()
        var totalSamples = 0

        jfrFiles
            .filter { it.exists() }
            .forEach { jfrFile ->
                log.info("Analyzing JFR file: ${jfrFile.absolutePath}")
                try {
                    RecordingFile.readAllEvents(jfrFile.toPath())
                        .asSequence()
                        .filter { it.eventType.name == "jdk.ExecutionSample" }
                        .forEach eventLoop@{ event ->
                            val stackTrace = event.stackTrace ?: return@eventLoop
                            totalSamples++
                            processStackTrace(stackTrace, testClasses, methodMappings)
                        }
                } catch (e: Exception) {
                    log.warn("Failed to read JFR file ${jfrFile.absolutePath}: ${e.message}")
                }
            }

        return MappingResult(methodMappings, totalSamples)
    }

    private fun processStackTrace(
        stackTrace: RecordedStackTrace,
        testClasses: Set<String>,
        methodMappings: MutableMap<MethodKey, MutableMap<String, TestHit>>
    ) {
        val frames = stackTrace.frames.reversed()

        for (testClass in testClasses) {
            val testFrameIndex = frames.indexOfFirst { it.method.type.name.contains(testClass) }
            if (testFrameIndex == -1) continue

            val testFrame = frames[testFrameIndex]
            val testId = "${testFrame.method.type.name}#${testFrame.method.name}"

            // Process frames after the test frame (called by the test)
            frames.drop(testFrameIndex + 1)
                .take(config.maxCallDepth)
                .filter { frame ->
                    val className = frame.method.type.name
                    !isExcludedPackage(className) && matchesIncludePattern(className)
                }
                .forEachIndexed { index, frame ->
                    val methodKey = MethodKey(
                        className = frame.method.type.name,
                        methodName = frame.method.name,
                        lineNumber = frame.lineNumber
                    )
                    val depth = index + 1

                    val testHits = methodMappings.getOrPut(methodKey) { mutableMapOf() }
                    val existingHit = testHits[testId]
                    if (existingHit != null) {
                        testHits[testId] = existingHit.copy(
                            samples = existingHit.samples + 1,
                            minDepth = minOf(existingHit.minDepth, depth)
                        )
                    } else {
                        testHits[testId] = TestHit(testId = testId, samples = 1, minDepth = depth)
                    }
                }
        }
    }

    private fun isExcludedPackage(className: String): Boolean {
        return EXCLUDED_PACKAGES.any { className.startsWith(it) }
    }

    private fun matchesIncludePattern(className: String): Boolean {
        val patterns = config.includePackages
        return patterns.isEmpty() || patterns.any { className.startsWith(it) }
    }

    private fun buildReport(result: MappingResult, testClasses: Set<String>): TestMappingReport {
        val methodMappings = result.methodMappings

        // Group by class
        val mappings = methodMappings.entries
            .groupBy { it.key.className }
            .mapValues { (_, entries) ->
                entries.associate { (methodKey, testHits) ->
                    methodKey.methodName to MethodMapping(
                        lineNumber = methodKey.lineNumber,
                        totalHits = testHits.values.sumOf { it.samples },
                        tests = testHits.values
                            .sortedByDescending { it.samples }
                            .map { hit ->
                                TestReference(
                                    id = hit.testId,
                                    depth = hit.minDepth,
                                    samples = hit.samples
                                )
                            }
                    )
                }
            }

        // Build hot methods list
        val hotMethods = methodMappings.entries
            .map { (methodKey, testHits) ->
                HotMethod(
                    method = "${methodKey.className}#${methodKey.methodName}",
                    totalHits = testHits.values.sumOf { it.samples },
                    testCount = testHits.size
                )
            }
            .sortedByDescending { it.totalHits }
            .take(config.topHotMethodsCount)

        return TestMappingReport(
            version = 1,
            generatedAt = Instant.now().toString(),
            summary = ReportSummary(
                totalTests = testClasses.size,
                totalMethods = methodMappings.size,
                totalSamples = result.totalSamples
            ),
            mappings = mappings,
            hotMethods = hotMethods
        )
    }

    private data class MethodKey(
        val className: String,
        val methodName: String,
        val lineNumber: Int
    )

    private data class TestHit(
        val testId: String,
        val samples: Int,
        val minDepth: Int
    )

    private data class MappingResult(
        val methodMappings: Map<MethodKey, Map<String, TestHit>>,
        val totalSamples: Int
    )

    private companion object {
        private val EXCLUDED_PACKAGES = listOf(
            "org.junit",
            "org.gradle",
            "jdk.internal",
            "sun.",
            "java.lang.reflect",
            "org.opentest4j",
            "worker.org.gradle"
        )
    }
}

/**
 * Configuration for the JFR analyzer.
 */
data class AnalyzerConfig(
    val maxCallDepth: Int = 20,
    val topHotMethodsCount: Int = 20,
    val includePackages: List<String> = emptyList()
)

/**
 * Complete test mapping report.
 */
data class TestMappingReport(
    val version: Int,
    val generatedAt: String,
    val summary: ReportSummary,
    val mappings: Map<String, Map<String, MethodMapping>>,
    val hotMethods: List<HotMethod>
) {
    fun toMap(): Map<String, Any> = mapOf(
        "version" to version,
        "generatedAt" to generatedAt,
        "summary" to mapOf(
            "totalTests" to summary.totalTests,
            "totalMethods" to summary.totalMethods,
            "totalSamples" to summary.totalSamples
        ),
        "mappings" to mappings.mapValues { (_, methods) ->
            methods.mapValues { (_, mapping) ->
                mapOf(
                    "lineNumber" to mapping.lineNumber,
                    "totalHits" to mapping.totalHits,
                    "tests" to mapping.tests.map { test ->
                        mapOf(
                            "id" to test.id,
                            "depth" to test.depth,
                            "samples" to test.samples
                        )
                    }
                )
            }
        },
        "hotMethods" to hotMethods.map { hot ->
            mapOf(
                "method" to hot.method,
                "totalHits" to hot.totalHits,
                "testCount" to hot.testCount
            )
        }
    )
}

data class ReportSummary(
    val totalTests: Int,
    val totalMethods: Int,
    val totalSamples: Int
)

data class MethodMapping(
    val lineNumber: Int,
    val totalHits: Int,
    val tests: List<TestReference>
)

data class TestReference(
    val id: String,
    val depth: Int,
    val samples: Int
)

data class HotMethod(
    val method: String,
    val totalHits: Int,
    val testCount: Int
)