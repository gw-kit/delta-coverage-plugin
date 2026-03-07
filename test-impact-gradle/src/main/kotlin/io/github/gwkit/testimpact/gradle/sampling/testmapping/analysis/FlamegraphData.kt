package io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis

/**
 * Collapsed stack data for flamegraph rendering.
 *
 * @property collapsedStacks map of "frame1;frame2;frame3" to sample count
 */
internal data class FlamegraphData(
    val collapsedStacks: Map<String, Int>,
)
