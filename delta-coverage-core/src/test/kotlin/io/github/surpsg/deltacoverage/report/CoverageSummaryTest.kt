package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CoverageSummaryTest {

    @Nested
    inner class InfoTest {

        @ParameterizedTest
        @CsvSource(
            "1, 3, 33.33",
            "1, 2, 50.00",
            "0, 0, 0.0",
            "0, 1, 0.0",
            "0, 3, 0.0",
        )
        fun `should return percents rounded to two digits`(
            covered: Int,
            total: Int,
            expectedPercents: Double
        ) {
            // GIVEN
            val info = CoverageSummary.Info(
                covered = covered,
                total = total,
                coverageEntity = CoverageEntity.INSTRUCTION
            )

            // WHEN
            val percents = info.percents

            // THEN
            percents shouldBe expectedPercents
        }
    }
}
