package io.github.surpsg.deltacoverage.sampling

/**
 * Configuration for stack sampling during test execution.
 *
 * @property intervalMs Sampling interval in milliseconds
 * @property maxDepth Maximum stack depth to capture
 * @property excludePackagePrefixes Package prefixes to exclude from stack frames
 */
data class SamplingConfig(
    val intervalMs: Long = DEFAULT_INTERVAL_MS,
    val maxDepth: Int = DEFAULT_MAX_DEPTH,
    val excludePackagePrefixes: Set<String> = DEFAULT_EXCLUDES,
) {
    init {
        require(intervalMs > 0) { "Sampling interval must be positive, was: $intervalMs" }
        require(maxDepth > 0) { "Max depth must be positive, was: $maxDepth" }
    }

    companion object {
        const val DEFAULT_INTERVAL_MS = 1L
        const val DEFAULT_MAX_DEPTH = 50

        val DEFAULT_EXCLUDES: Set<String> = setOf(
            "java.",
            "javax.",
            "jdk.",
            "sun.",
            "com.sun.",
            "org.junit.",
            "org.gradle.",
            "worker.org.gradle.",
            "org.testng.",
            "kotlin.",
            "kotlinx.",
            "io.mockk.",
            "io.kotest.",
            "org.mockito.",
            "io.github.surpsg.deltacoverage.sampling.",
        )
    }
}
