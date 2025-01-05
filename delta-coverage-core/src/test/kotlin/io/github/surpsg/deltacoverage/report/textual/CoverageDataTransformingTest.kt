package io.github.surpsg.deltacoverage.report.textual

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test

class CoverageDataTransformingTest {

    @Test
    fun `should return list as is when no lambda classes`() {
        // GIVEN
        val data: List<RawCoverageData> = listOf(
            rawCoverageData("com.example.Class1"),
            rawCoverageData("com.example.Class2"),
        )

        // WHEN
        val actual: List<RawCoverageData> = CoverageDataTransforming.transform(data)

        // THEN
        actual shouldContainExactlyInAnyOrder data
    }

    @Test
    fun `should merge lambda classes`() {
        // GIVEN
        val data: List<RawCoverageData> = listOf(
            rawCoverageData("com.example.Class2"),
            rawCoverageData("com.example.Class1.new Function() {...}"),
            rawCoverageData("com.example.Class1.new Function2() {...}"),
            rawCoverageData("com.example.Class1.new Function1() {...}"),
        )

        // WHEN
        val actual: List<RawCoverageData> = CoverageDataTransforming.transform(data)

        // THEN
        actual shouldContainExactlyInAnyOrder listOf(
            rawCoverageData("com.example.Class2"),
            RawCoverageData {
                aClass = "com.example.Class1"
                instr(3, 3)
                lines(3, 3)
                branches(3, 3)
            }
        )
    }

    private fun rawCoverageData(aClass: String) = RawCoverageData {
        this.aClass = aClass
        instr(1, 1)
        lines(1, 1)
        branches(1, 1)
    }
}
