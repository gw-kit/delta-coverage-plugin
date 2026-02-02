package io.github.surpsg.deltacoverage.gradle.sampling

import io.github.surpsg.deltacoverage.gradle.utils.booleanProperty
import io.github.surpsg.deltacoverage.gradle.utils.new
import io.github.surpsg.deltacoverage.gradle.utils.stringProperty
import io.github.surpsg.deltacoverage.sampling.SamplingConfig
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import java.nio.file.Paths
import javax.inject.Inject

/**
 * Configuration for test-to-code mapping via stack sampling.
 *
 * Example usage:
 * ```kotlin
 * deltaCoverageReport {
 *     testMapping {
 *         enabled = true
 *         sampling {
 *             intervalMs = 1
 *             maxDepth = 50
 *         }
 *         output {
 *             samplesFile = "build/reports/delta-coverage/test-samples.json"
 *         }
 *     }
 * }
 * ```
 */
open class TestMappingConfiguration @Inject constructor(
    objectFactory: ObjectFactory,
) {
    /**
     * Enables or disables test-to-code mapping.
     * Defaults to false.
     */
    @Input
    val enabled: Property<Boolean> = objectFactory.booleanProperty(false)

    /**
     * Sampling configuration.
     */
    @Nested
    val sampling: SamplingConfiguration = objectFactory.new<SamplingConfiguration>()

    /**
     * Output configuration.
     */
    @Nested
    val output: OutputConfiguration = objectFactory.new<OutputConfiguration>()

    /**
     * Configures sampling settings.
     */
    fun sampling(action: Action<in SamplingConfiguration>) {
        action.execute(sampling)
    }

    /**
     * Configures output settings.
     */
    fun output(action: Action<in OutputConfiguration>) {
        action.execute(output)
    }

    override fun toString(): String = "TestMappingConfiguration(" +
            "enabled=${enabled.get()}, " +
            "sampling=$sampling, " +
            "output=$output)"
}

/**
 * Configuration for the stack sampling process.
 */
open class SamplingConfiguration @Inject constructor(
    objectFactory: ObjectFactory,
) {
    /**
     * Sampling interval in milliseconds.
     * Defaults to 1ms.
     */
    @Input
    val intervalMs: Property<Long> = objectFactory
        .property(Long::class.javaObjectType)
        .convention(SamplingConfig.DEFAULT_INTERVAL_MS)

    /**
     * Maximum stack depth to capture.
     * Defaults to 50.
     */
    @Input
    val maxDepth: Property<Int> = objectFactory
        .property(Int::class.javaObjectType)
        .convention(SamplingConfig.DEFAULT_MAX_DEPTH)

    /**
     * Package prefixes to exclude from stack frames.
     * Defaults to common framework packages.
     */
    @Input
    val excludePackagePrefixes: SetProperty<String> = objectFactory
        .setProperty(String::class.java)
        .convention(SamplingConfig.DEFAULT_EXCLUDES)

    /**
     * Converts this configuration to a core SamplingConfig.
     */
    internal fun toSamplingConfig(): SamplingConfig = SamplingConfig(
        intervalMs = intervalMs.get(),
        maxDepth = maxDepth.get(),
        excludePackagePrefixes = excludePackagePrefixes.get(),
    )

    override fun toString(): String = "SamplingConfiguration(" +
            "intervalMs=${intervalMs.get()}, " +
            "maxDepth=${maxDepth.get()})"
}

/**
 * Configuration for test mapping output.
 */
open class OutputConfiguration @Inject constructor(
    objectFactory: ObjectFactory,
) {
    /**
     * Path to the output samples JSON file.
     * Defaults to build/reports/delta-coverage/test-samples.json
     */
    @Input
    val samplesFile: Property<String> = objectFactory.stringProperty {
        Paths.get("build", "reports", "delta-coverage", "test-samples.json").toString()
    }

    override fun toString(): String = "OutputConfiguration(samplesFile='${samplesFile.get()}')"
}
