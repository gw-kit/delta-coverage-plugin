package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.core.internal.analysis.filter.Filters
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class FilteringAnalyzerTest {

    @Test
    fun `analyzeClass should throw if cannot read class file bytes`() {
        // GIVEN
        val filteringAnalyzer = FilteringAnalyzer(
            executionData = ExecutionDataStore(),
            coverageVisitor = {},
            classFilter = { true },
            customFilterProvider = { Filters.NONE }
        )

        // WHEN // THEN
        assertThrows<IOException> {
            filteringAnalyzer.analyzeClass(ByteArray(0), "any")
        }
    }
}
