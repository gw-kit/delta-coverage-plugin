package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.CoverageEngine as CoreCoverageEngine

enum class CoverageEngine {
    JACOCO,
    INTELLIJ;

    internal fun asCoreEngine(): CoreCoverageEngine = when (this) {
        JACOCO -> CoreCoverageEngine.JACOCO
        INTELLIJ -> CoreCoverageEngine.INTELLIJ
    }
}
