package io.github.surpsg.deltacoverage.report.violation

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ViolationResolveContextTest {

    @Test
    fun `isIgnoredByThreshold should return true if total is less than threshold`() {
        // GIVEN
        val violationResolveContext = ViolationResolveContext(null, 10, 9)

        // WHEN
        val actual = violationResolveContext.isIgnoredByThreshold()

        // THEN
        actual shouldBe true
    }

    @Test
    fun `isIgnoredByThreshold should return false if total is greater than threshold`() {
        // GIVEN
        val violationResolveContext = ViolationResolveContext(null, 23, 31)

        // WHEN
        val actual = violationResolveContext.isIgnoredByThreshold()

        // THEN
        actual shouldBe false
    }
}
