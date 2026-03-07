package io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis

import jdk.jfr.consumer.RecordedFrame
import jdk.jfr.consumer.RecordedMethod
import jdk.jfr.consumer.RecordedStackTrace
import jdk.jfr.consumer.RecordingFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.reflect.Modifier
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
                log.info("Analyzing JFR file: {}", jfrFile.absolutePath)
                try {
                    RecordingFile.readAllEvents(jfrFile.toPath())
                        .asSequence()
                        .filter { it.eventType.name == "jdk.ExecutionSample" }
                        .forEach eventLoop@{ event ->
                            val stackTrace = event.stackTrace ?: return@eventLoop
                            totalSamples++
                            processStackTrace(stackTrace, testClasses, methodMappings)
                        }
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    log.warn("Failed to read JFR file {}: {}", jfrFile.absolutePath, e.message)
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

            val testFrame: RecordedFrame = frames[testFrameIndex]

            val testId = "${testFrame.method.type.name}#${testFrame.method.name}"

            // Process frames after the test frame (called by the test)
            frames.drop(testFrameIndex + 1)
                .take(config.maxCallDepth)
                .filter { frame -> !Modifier.isPrivate(frame.method.modifiers) }
                .filter { frame ->
                    val className = frame.method.type.name
                    !isExcludedPackage(className) && matchesIncludePattern(className)
                }
                .forEachIndexed { index, frame ->
                    val method: RecordedMethod = frame.method
                    val methodKey = MethodKey(
                        className = method.type.name,
                        methodName = method.name,
                        descriptor = method.descriptor,
                        modifiers = method.modifiers,
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
        return EXCLUDED_PACKAGES.any { className.startsWith(it) } ||
            config.excludePackages.any { className.startsWith(it) }
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
                    val methodWithSignature = "${methodKey.methodName}${descriptorToSignature(methodKey.descriptor)}"
                    methodWithSignature to MethodMapping(
                        signature = descriptorToSignature(methodKey.descriptor),
                        visibility = modifiersToVisibility(methodKey.modifiers),
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
                val methodWithSignature = "${methodKey.methodName}${descriptorToSignature(methodKey.descriptor)}"
                HotMethod(
                    method = "${methodKey.className}#$methodWithSignature",
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
                totalSamples = result.totalSamples,
                maxCallDepth = config.maxCallDepth
            ),
            mappings = mappings,
            hotMethods = hotMethods
        )
    }

    private data class MethodKey(
        val className: String,
        val methodName: String,
        val descriptor: String,
        val modifiers: Int,
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

        /**
         * Converts JVM modifiers to visibility string.
         */
        fun modifiersToVisibility(modifiers: Int): String = when {
            Modifier.isPublic(modifiers) -> "public"
            Modifier.isPrivate(modifiers) -> "private"
            Modifier.isProtected(modifiers) -> "protected"
            else -> "package-private"
        }

        /**
         * Converts JVM method descriptor to human-readable signature.
         * Example: "(II)I" -> "(int, int)"
         * Example: "(Ljava/lang/String;I)V" -> "(String, int)"
         */
        fun descriptorToSignature(descriptor: String): String {
            val params = parseDescriptorParams(descriptor)
            return "(${params.joinToString(", ")})"
        }

        private fun parseDescriptorParams(descriptor: String): List<String> {
            if (!descriptor.startsWith("(")) return emptyList()

            val params = mutableListOf<String>()
            var i = 1 // skip opening '('

            while (i < descriptor.length && descriptor[i] != ')') {
                val (type, newIndex) = parseType(descriptor, i)
                params.add(type)
                i = newIndex
            }
            return params
        }

        @Suppress("CyclomaticComplexMethod")
        private fun parseType(descriptor: String, startIndex: Int): Pair<String, Int> {
            var i = startIndex
            var arrayDepth = 0

            // Count array dimensions
            while (i < descriptor.length && descriptor[i] == '[') {
                arrayDepth++
                i++
            }

            val (baseType, nextIndex) = when (descriptor[i]) {
                'B' -> "byte" to i + 1
                'C' -> "char" to i + 1
                'D' -> "double" to i + 1
                'F' -> "float" to i + 1
                'I' -> "int" to i + 1
                'J' -> "long" to i + 1
                'S' -> "short" to i + 1
                'Z' -> "boolean" to i + 1
                'V' -> "void" to i + 1
                'L' -> {
                    // Object type: Ljava/lang/String;
                    val semicolonIndex = descriptor.indexOf(';', i)
                    val fullClassName = descriptor.substring(i + 1, semicolonIndex).replace('/', '.')
                    val simpleName = fullClassName.substringAfterLast('.')
                    simpleName to semicolonIndex + 1
                }
                else -> "?" to i + 1
            }

            val typeName = if (arrayDepth > 0) {
                baseType + "[]".repeat(arrayDepth)
            } else {
                baseType
            }

            return typeName to nextIndex
        }
    }
}

/**
 * Configuration for the JFR analyzer.
 */
data class AnalyzerConfig(
    val maxCallDepth: Int = 20,
    val topHotMethodsCount: Int = 20,
    val includePackages: List<String> = emptyList(),
    val excludePackages: List<String> = emptyList()
)

/**
 * Complete test mapping report.
 */
internal data class TestMappingReport(
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
            "totalSamples" to summary.totalSamples,
            "maxCallDepth" to summary.maxCallDepth
        ),
        "mappings" to mappings.mapValues { (_, methods) ->
            methods.mapValues { (_, mapping) ->
                mapOf(
                    "signature" to mapping.signature,
                    "visibility" to mapping.visibility,
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

internal data class ReportSummary(
    val totalTests: Int,
    val totalMethods: Int,
    val totalSamples: Int,
    val maxCallDepth: Int
)

internal data class MethodMapping(
    val signature: String,
    val visibility: String,
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
