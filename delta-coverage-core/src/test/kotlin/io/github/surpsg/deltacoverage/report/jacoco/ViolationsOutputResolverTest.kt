package io.github.surpsg.deltacoverage.report.jacoco

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.report.jacoco.verification.ViolationsOutputResolver
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.mockk
import org.jacoco.core.analysis.CoverageNodeImpl
import org.jacoco.core.analysis.ICounter
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.core.internal.analysis.CounterImpl
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

class ViolationsOutputResolverTest {

    @Test
    fun `should return empty violation rules initially`() {
        // GIVEN
        val violationsOutputResolver = ViolationsOutputResolver(mockk())

        // WHEN
        val actualViolations: Map<CoverageEntity, String> = violationsOutputResolver.getViolations()

        // THEN
        actualViolations.shouldBeEmpty()
    }

    @ParameterizedTest
    @EnumSource(CoverageEntity::class)
    fun `should return violation message if rule violated`(entity: CoverageEntity) {
        // GIVEN

        val violationsOutputResolver = ViolationsOutputResolver(
            CoverageRulesConfig {
                violationRules += ViolationRule {
                    minCoverageRatio = 0.1
                    coverageEntity = entity
                }
            }
        )
        val node: CoverageNodeImpl = buildCoverageNode()
        val limit: Limit = ICoverageNode.CounterEntity.valueOf(entity.name).buildLimit()

        // WHEN
        violationsOutputResolver.onViolation(node, Rule(), limit, COVERAGE_ERROR_MSG)

        // THEN
        assertSoftly(violationsOutputResolver.getViolations()) {
            shouldHaveSize(1)
            shouldContain(entity to COVERAGE_ERROR_MSG)
        }
    }

    @Test
    fun `should ignore violation if coverage entity is not supported`() {
        // GIVEN
        val violationsOutputResolver = ViolationsOutputResolver(
            CoverageRulesConfig {
                violationRules += ViolationRule {
                    minCoverageRatio = 0.1
                    coverageEntity = CoverageEntity.INSTRUCTION
                }
            }
        )
        val node: CoverageNodeImpl = buildCoverageNode()
        val limit: Limit = ICoverageNode.CounterEntity.CLASS.buildLimit()

        // WHEN
        violationsOutputResolver.onViolation(node, Rule(), limit, COVERAGE_ERROR_MSG)

        // THEN
        violationsOutputResolver.getViolations().shouldBeEmpty()
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "LINE, 100, 1",
            "INSTRUCTION, 100, 100",
            "BRANCH, 1, 1",
        ]
    )
    fun `should return violation message if rule violated and total count is greater than or equal to threshold`(
        entity: CoverageEntity,
        totalCount: Int,
        threshold: Int
    ) {
        // GIVEN
        val violationsOutputResolver = ViolationsOutputResolver(
            CoverageRulesConfig {
                violationRules += ViolationRule {
                    minCoverageRatio = 0.2
                    coverageEntity = entity
                    entityCountThreshold = threshold
                }
            }
        )
        val coverageNode: CoverageNodeImpl = buildCoverageNode(totalCount)
        val limit: Limit = ICoverageNode.CounterEntity.valueOf(entity.name).buildLimit()

        // WHEN
        violationsOutputResolver.onViolation(coverageNode, Rule(), limit, COVERAGE_ERROR_MSG)

        // THEN
        assertSoftly(violationsOutputResolver.getViolations()) {
            shouldHaveSize(1)
            shouldContain(entity to COVERAGE_ERROR_MSG)
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "LINE, 1, 2",
            "INSTRUCTION, 100, 101",
            "BRANCH, 1, 100",
        ]
    )
    fun `should return empty violations if rule violated and total count does not reach threshold`(
        entity: CoverageEntity,
        totalCount: Int,
        threshold: Int
    ) {
        // GIVEN
        val violationsOutputResolver = ViolationsOutputResolver(
            CoverageRulesConfig {
                violationRules += ViolationRule {
                    minCoverageRatio = 0.3
                    coverageEntity = entity
                    entityCountThreshold = threshold
                }
            }
        )
        val coverageNode: CoverageNodeImpl = buildCoverageNode(totalCount)
        val limit: Limit = ICoverageNode.CounterEntity.valueOf(entity.name).buildLimit()

        // WHEN
        violationsOutputResolver.onViolation(coverageNode, Rule(), limit, COVERAGE_ERROR_MSG)

        // THEN
        violationsOutputResolver.getViolations().shouldBeEmpty()
    }

    @ParameterizedTest
    @EnumSource(value = CoverageEntity::class)
    fun `should not ignore violation if delta violation rule is not found by jacoco entity`(
        entity: CoverageEntity,
    ) {
        // GIVEN
        val violationsOutputResolver = ViolationsOutputResolver(CoverageRulesConfig())
        val coverageNode: CoverageNodeImpl = buildCoverageNode(1)
        val limit: Limit = ICoverageNode.CounterEntity.valueOf(entity.name).buildLimit()

        // WHEN
        violationsOutputResolver.onViolation(coverageNode, Rule(), limit, COVERAGE_ERROR_MSG)

        // THEN
        assertSoftly(violationsOutputResolver.getViolations()) {
            shouldHaveSize(1)
            shouldContain(entity to COVERAGE_ERROR_MSG)
        }
    }

    private fun buildCoverageNode(totalCount: Int = 0): CoverageNodeImpl {
        return object : CoverageNodeImpl(ICoverageNode.ElementType.CLASS, ICoverageNode.ElementType.CLASS.name) {
            override fun getInstructionCounter(): ICounter = CounterImpl.getInstance(0, totalCount)

            override fun getBranchCounter(): ICounter = CounterImpl.getInstance(0, totalCount)

            override fun getLineCounter(): ICounter = CounterImpl.getInstance(0, totalCount)
        }
    }

    private fun ICoverageNode.CounterEntity.buildLimit(): Limit {
        val thisNodeEntity = this
        return Limit().apply { setCounter(thisNodeEntity.name) }
    }

    companion object {
        const val COVERAGE_ERROR_MSG = "pure coverage test error msg"
    }
}
