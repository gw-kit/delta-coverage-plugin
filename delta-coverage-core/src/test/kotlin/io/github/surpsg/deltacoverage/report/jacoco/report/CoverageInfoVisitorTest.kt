package io.github.surpsg.deltacoverage.report.jacoco.report

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.jacoco.core.internal.analysis.BundleCoverageImpl
import org.jacoco.core.internal.analysis.ClassCoverageImpl
import org.jacoco.core.internal.analysis.CounterImpl
import org.jacoco.core.internal.analysis.MethodCoverageImpl
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoverageInfoVisitorTest {

    @Nested
    inner class DefaultCoverageInfoVisitorTest {

        @Test
        fun `should collect coverage info`() {
            // GIVEN
            val infoVisitor = CoverageInfoVisitor.create()
            val classCoverage = ClassCoverageImpl("some.class", 1234L, false).apply {
                val method = MethodCoverageImpl("some.method", "any desc", "any signature").apply {
                    increment(
                        CounterImpl.getInstance(0, 2),
                        CounterImpl.getInstance(0, 1),
                        1,
                    )
                    increment(
                        CounterImpl.getInstance(1, 0),
                        CounterImpl.getInstance(0, 0),
                        2,
                    )
                }
                addMethod(method)
            }

            // WHEN
            infoVisitor.visitBundle(
                BundleCoverageImpl("test", listOf(classCoverage), emptyList()),
                null,
            )

            // THEN
            infoVisitor.coverageSummary.shouldContainExactlyInAnyOrder(
                CoverageSummary.Info(coverageEntity = CoverageEntity.INSTRUCTION, covered = 2, total = 3),
                CoverageSummary.Info(coverageEntity = CoverageEntity.LINE, covered = 1, total = 2),
                CoverageSummary.Info(coverageEntity = CoverageEntity.BRANCH, covered = 1, total = 1),
            )
        }
    }
}
