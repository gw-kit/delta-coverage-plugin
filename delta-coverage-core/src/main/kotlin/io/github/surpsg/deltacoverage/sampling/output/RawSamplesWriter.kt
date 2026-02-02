package io.github.surpsg.deltacoverage.sampling.output

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.surpsg.deltacoverage.sampling.Sample
import io.github.surpsg.deltacoverage.sampling.SamplingConfig
import java.nio.file.Path
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * Writes raw stack samples to a JSON file.
 */
object RawSamplesWriter {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .enable(SerializationFeature.INDENT_OUTPUT)

    /**
     * Writes samples to the specified output file.
     *
     * @param outputPath Path to the output JSON file
     * @param samples List of samples to write
     * @param config Sampling configuration used during collection
     */
    fun write(
        outputPath: Path,
        samples: List<Sample>,
        config: SamplingConfig,
    ) {
        outputPath.parent?.createDirectories()

        val output = SamplesOutput(
            version = OUTPUT_VERSION,
            generatedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
            samplingIntervalMs = config.intervalMs,
            maxDepth = config.maxDepth,
            totalSamples = samples.size,
            samples = samples.map { it.toOutputSample() },
        )

        val json = objectMapper.writeValueAsString(output)
        outputPath.writeText(json)
    }

    private fun Sample.toOutputSample(): OutputSample = OutputSample(
        timestamp = timestamp,
        testId = testId.toCompactString(),
        threadName = threadName,
        frames = frames.map { frame ->
            OutputFrame(
                `class` = frame.className,
                method = frame.methodName,
                line = frame.lineNumber,
            )
        },
    )

    private const val OUTPUT_VERSION = 1
}

internal data class SamplesOutput(
    val version: Int,
    val generatedAt: String,
    val samplingIntervalMs: Long,
    val maxDepth: Int,
    val totalSamples: Int,
    val samples: List<OutputSample>,
)

internal data class OutputSample(
    val timestamp: Long,
    val testId: String,
    val threadName: String,
    val frames: List<OutputFrame>,
)

internal data class OutputFrame(
    val `class`: String,
    val method: String,
    val line: Int,
)
