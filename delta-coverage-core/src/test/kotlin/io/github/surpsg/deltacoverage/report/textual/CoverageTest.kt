package io.github.surpsg.deltacoverage.report.textual

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.report.textual.Coverage.Companion.has
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class CoverageTest {

    @ParameterizedTest
    @EnumSource(CoverageEntity::class)
    fun `should successfully create coverage`(
        entity: CoverageEntity
    ) {
        // GIVEN // WHEN
        val coverage = entity.has(10, 20)

        // THEN
        assertSoftly(coverage) {
            this.entity shouldBe entity
            covered shouldBe 10
            total shouldBe 20
            ratio shouldBe 0.5
        }
    }

    @Test
    fun `should throw if merge coverage with different entity`() {
        // GIVEN
        val coverage1 = CoverageEntity.LINE.has(10, 20)
        val coverage2 = CoverageEntity.BRANCH.has(5, 10)

        // WHEN // THEN
        assertSoftly {
            shouldThrow<IllegalArgumentException> {
                coverage1.mergeWith(coverage2)
            }
        }
    }

    @Test
    fun `should merge coverage successfully`() {
        // GIVEN
        val coverage1 = CoverageEntity.LINE.has(10, 20)
        val coverage2 = CoverageEntity.LINE.has(5, 10)

        // WHEN
        val mergedCoverage = coverage1.mergeWith(coverage2)

        // THEN
        assertSoftly(mergedCoverage) {
            entity shouldBe CoverageEntity.LINE
            covered shouldBe 15
            total shouldBe 30
            ratio shouldBe 0.5
        }
    }
}
