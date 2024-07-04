package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.exception.CoverageViolatedException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class CoverageViolationsPropagatorTest {
    private val propagator = CoverageViolationsPropagator()

    @Test
    fun `propagate should throw CoverageViolatedException if failOnViolation is true and there are violations`() {
        // GIVEN
        val config = CoverageRulesConfig {
            failOnViolation = true
        }
        val violations = listOf("violation1", "violation2")

        // WHEN // THEN
        shouldThrow<CoverageViolatedException> {
            propagator.propagate("any view", config, violations)
        }
    }

    @Test
    fun `propagate should not throw any exception if failOnViolation is true and there are no violations`() {
        // GIVEN
        val config = CoverageRulesConfig {
            failOnViolation = true
        }
        val violations = emptyList<String>()

        // WHEN // THEN
        shouldNotThrow<CoverageViolatedException> {
            propagator.propagate("any view", config, violations)
        }
    }

    @Test
    fun `propagate should not throw any exception if failOnViolation is false`() {
        // GIVEN
        val config = CoverageRulesConfig {
            failOnViolation = false
        }
        val violations = listOf("violation1", "violation2")

        // WHEN // THEN
        shouldNotThrow<CoverageViolatedException> {
            propagator.propagate("any view", config, violations)
        }
    }
}
