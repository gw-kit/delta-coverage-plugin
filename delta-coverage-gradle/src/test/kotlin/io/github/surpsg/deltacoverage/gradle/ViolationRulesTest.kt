package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.maps.shouldContainKey
import org.gradle.api.Project
import org.junit.jupiter.api.Test

class ViolationRulesTest {

    private val project: Project = testJavaProject()

    @Test
    fun `failOnCoverageLessThan should set all coverage values to a single value and failOnViolation is set to true`() {
        val expectedCoverage = 0.9
        val actualViolationRules = ViolationRules(project.objects).apply {
            failIfCoverageLessThan(expectedCoverage)
        }

        assertSoftly {
            actualViolationRules.failOnViolation.get() shouldBeEqualComparingTo true

            val allRules: MutableMap<CoverageEntity, ViolationRule> = actualViolationRules.rules.get()
            CoverageEntity.entries.forEach { coverageEntity ->
                allRules shouldContainKey coverageEntity
                allRules.getValue(coverageEntity).minCoverageRatio.get() shouldBeEqualComparingTo expectedCoverage
            }
        }
    }
}
