package io.github.surpsg.deltacoverage.gradle.sampling.listener

import io.github.surpsg.deltacoverage.sampling.Sample
import io.github.surpsg.deltacoverage.sampling.SamplingConfig
import io.github.surpsg.deltacoverage.sampling.StackSampler
import io.github.surpsg.deltacoverage.sampling.TestIdentifier
import io.github.surpsg.deltacoverage.sampling.output.RawSamplesWriter
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier as JUnitTestIdentifier
import org.junit.platform.launcher.TestPlan
import org.slf4j.LoggerFactory
import java.nio.file.Paths

/**
 * JUnit Platform TestExecutionListener that performs stack sampling during test execution.
 *
 * This listener is registered via ServiceLoader and activated via system properties.
 */
class SamplingTestListener : TestExecutionListener {

    init {
        // This will print when the class is instantiated by ServiceLoader
        println("[SamplingTestListener] Instance created - listener loaded successfully")
    }

    private var sampler: StackSampler? = null
    private var config: SamplingConfig? = null
    private var outputFile: String? = null
    private var enabled: Boolean = false

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        log.info("testPlanExecutionStarted called")

        println("aaaaaaaaaaaaaaaaaaaaaaaaa")

        val enabledProp = System.getProperty(PROP_ENABLED)
        enabled = enabledProp?.toBoolean() ?: false
        log.info("Sampling enabled: {} (property value: '{}')", enabled, enabledProp)

        if (!enabled) {
            log.info("Sampling is disabled, skipping initialization")
            return
        }

        config = buildConfig()
        outputFile = System.getProperty(PROP_OUTPUT_FILE)
        log.info("Configuration: intervalMs={}, maxDepth={}, outputFile={}",
            config?.intervalMs, config?.maxDepth, outputFile)

        sampler = StackSampler(config!!).also { it.start() }
        log.info("Stack sampler started with interval={}ms, maxDepth={}",
            config?.intervalMs, config?.maxDepth)
    }

    override fun executionStarted(testIdentifier: JUnitTestIdentifier) {
        if (!enabled || !testIdentifier.isTest) {
            return
        }

        val testId = testIdentifier.toTestIdentifier()
        log.info("Test started: {}#{}", testId.className, testId.methodName)
        sampler?.setCurrentTest(testId)
    }

    override fun executionFinished(testIdentifier: JUnitTestIdentifier, testExecutionResult: TestExecutionResult) {
        if (!enabled || !testIdentifier.isTest) {
            return
        }

        log.info("Test finished: {} (status: {})", testIdentifier.displayName, testExecutionResult.status)
        sampler?.clearCurrentTest()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        log.info("testPlanExecutionFinished called, enabled={}", enabled)
        if (!enabled) {
            return
        }

        val samples: List<Sample> = sampler?.stop() ?: emptyList()
        log.info("Stack sampler stopped, collected {} samples", samples.size)

        val output = outputFile
        val samplingConfig = config
        if (output != null && samplingConfig != null) {
            log.info("Writing samples to: {}", output)
            try {
                RawSamplesWriter.write(
                    outputPath = Paths.get(output),
                    samples = samples,
                    config = samplingConfig,
                )
                log.info("Successfully wrote {} samples to {}", samples.size, output)
            } catch (e: Exception) {
                log.error("Failed to write samples to {}: {}", output, e.message, e)
            }
        } else {
            log.warn("Cannot write samples: outputFile={}, config={}", output, samplingConfig)
        }
    }

    private fun buildConfig(): SamplingConfig {
        val intervalMs = System.getProperty(PROP_INTERVAL_MS)?.toLongOrNull()
            ?: SamplingConfig.DEFAULT_INTERVAL_MS
        val maxDepth = System.getProperty(PROP_MAX_DEPTH)?.toIntOrNull()
            ?: SamplingConfig.DEFAULT_MAX_DEPTH
        val excludePrefixes = System.getProperty(PROP_EXCLUDE_PREFIXES)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: SamplingConfig.DEFAULT_EXCLUDES

        return SamplingConfig(
            intervalMs = intervalMs,
            maxDepth = maxDepth,
            excludePackagePrefixes = excludePrefixes,
        )
    }

    private fun JUnitTestIdentifier.toTestIdentifier(): TestIdentifier {
        val source = source.orElse(null)
        val className = extractClassName(source)
        val methodName = extractMethodName(source)

        return TestIdentifier(
            className = className,
            methodName = methodName,
            displayName = displayName,
        )
    }

    private fun extractClassName(source: Any?): String {
        if (source == null) return UNKNOWN_CLASS

        return try {
            val methodSource = source as? org.junit.platform.engine.support.descriptor.MethodSource
            methodSource?.className ?: UNKNOWN_CLASS
        } catch (_: Exception) {
            UNKNOWN_CLASS
        }
    }

    private fun extractMethodName(source: Any?): String {
        if (source == null) return UNKNOWN_METHOD

        return try {
            val methodSource = source as? org.junit.platform.engine.support.descriptor.MethodSource
            methodSource?.methodName ?: UNKNOWN_METHOD
        } catch (_: Exception) {
            UNKNOWN_METHOD
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SamplingTestListener::class.java)

        private const val PROP_ENABLED = "delta.coverage.sampling.enabled"
        private const val PROP_INTERVAL_MS = "delta.coverage.sampling.intervalMs"
        private const val PROP_MAX_DEPTH = "delta.coverage.sampling.maxDepth"
        private const val PROP_EXCLUDE_PREFIXES = "delta.coverage.sampling.excludePrefixes"
        private const val PROP_OUTPUT_FILE = "delta.coverage.sampling.outputFile"

        private const val UNKNOWN_CLASS = "<unknown>"
        private const val UNKNOWN_METHOD = "<unknown>"
    }
}
