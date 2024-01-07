package io.github.surpsg.deltacoverage.gradle

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class ViolationRulesTest : StringSpec({

    val project: Project = ProjectBuilder.builder().build()

    "failOnCoverageLessThan should set all coverage values to a single value and set failOnViolation=true" {
        val expectedCoverage = 0.9
        val actualViolationRules = ViolationRules(project.objects).apply {
            failIfCoverageLessThan(expectedCoverage)
        }

        assertSoftly {
            actualViolationRules.failOnViolation.get() shouldBeEqualComparingTo true
            actualViolationRules.minBranches.get() shouldBeEqualComparingTo expectedCoverage
            actualViolationRules.minInstructions.get() shouldBeEqualComparingTo expectedCoverage
            actualViolationRules.minLines.get() shouldBeEqualComparingTo expectedCoverage
        }
    }
})
