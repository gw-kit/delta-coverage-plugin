package io.github.surpsg.deltacoverage.gradle

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import io.github.surpsg.deltacoverage.CoverageEngine as CoreEngine
import io.github.surpsg.deltacoverage.gradle.CoverageEngine as GradleEngine

class CoverageEngineTest {

    @ParameterizedTest
    @CsvSource(
        "JACOCO, JACOCO",
        "INTELLIJ, INTELLIJ",
    )
    fun `should return corresponding core coverage engine`(
        // GIVEN
        gradleEngine: GradleEngine,
        coreEngine: CoreEngine,
    ) {
        // WHEN // THEN
        gradleEngine.asCoreEngine() shouldBe coreEngine
    }
}
