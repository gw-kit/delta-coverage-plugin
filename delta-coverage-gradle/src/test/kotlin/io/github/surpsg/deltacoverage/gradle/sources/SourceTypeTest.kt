package io.github.surpsg.deltacoverage.gradle.sources

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SourceTypeTest {

    @ParameterizedTest
    @CsvSource(
        "KOVER, CLASSES, .class files",
        "KOVER, SOURCES, sources",

        "DELTA_COVERAGE, CLASSES, .class files",
        "DELTA_COVERAGE, SOURCES, sources",

        "JACOCO, CLASSES, .class files",
        "JACOCO, SOURCES, sources",
    )
    fun `should return resource name`(
        sourceProvider: SourcesResolver.Provider,
        sourceType: SourceType,
        expectedResourceName: String,
    ) {
        // WHEN
        val actualResourceName = sourceType.resourceName(sourceProvider)

        // THEN
        actualResourceName shouldBe expectedResourceName
    }

    @ParameterizedTest
    @CsvSource(
        "KOVER, '.ic' files",
        "JACOCO, '.exec' files",
        "DELTA_COVERAGE, deltaCoverageReport.reportViews.<view>.binaryCoverageFiles",
    )
    fun `should return resource name for binary coverage`(
        sourceProvider: SourcesResolver.Provider,
        expectedResourceName: String,
    ) {
        // WHEN
        val actualResourceName = SourceType.COVERAGE_BINARIES.resourceName(sourceProvider)

        // THEN
        actualResourceName shouldBe expectedResourceName
    }
}
