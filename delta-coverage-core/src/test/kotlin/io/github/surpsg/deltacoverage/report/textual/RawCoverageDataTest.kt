package io.github.surpsg.deltacoverage.report.textual

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class RawCoverageDataTest {

    @ParameterizedTest
    @CsvSource(
        "5, 10, 0.5",
        "5, 20, 0.25",
        "0, 10, 0.0",
        "0, 0, NaN",
    )
    fun `should return correct ration for branches`(
        covered: Int,
        total: Int,
        expected: Double,
    ) {
        // GIVEN
        val data = RawCoverageData.newBlank {
            branches(covered = covered, total = total)
        }

        // WHEN // THEN
        data.branches.ratio shouldBe expected
    }

    @ParameterizedTest
    @CsvSource(
        "10, 100, 0.1",
        "2, 8, 0.25",
        "7, 10, 0.7",
    )
    fun `should return correct ration for lines`(
        covered: Int,
        total: Int,
        expected: Double,
    ) {
        // GIVEN
        val data = RawCoverageData.newBlank {
            lines(covered = covered, total = total)
        }

        // WHEN // THEN
        data.lines.ratio shouldBe expected
    }

    @ParameterizedTest
    @CsvSource(
        "20, 100, 0.2",
        "4, 16, 0.25",
        "10, 10, 1.0",
    )
    fun `should return correct ration for instructions`(
        covered: Int,
        total: Int,
        expected: Double,
    ) {
        // GIVEN
        val data = RawCoverageData.newBlank {
            instr(covered = covered, total = total)
        }

        // WHEN // THEN
        data.instr.ratio shouldBe expected
    }

    @Test
    fun `merge should correctly merge two RawCoverageData instances`() {
        // GIVEN
        val data1 = RawCoverageData {
            aClass = "class1"
            instr(4, 4)
            branches(5, 10)
            lines(5, 10)
        }

        val data2 = RawCoverageData {
            aClass = "class2"
            instr(2, 6)
            branches(5, 10)
            lines(5, 10)
        }

        // WHEN
        val mergedData = data1.merge(data2)

        // THEN
        mergedData shouldBeEqualToComparingFields RawCoverageData {
            aClass = "class1"
            instr(6, 10)
            branches(10, 20)
            lines(10, 20)
        }
    }

    @Test
    fun `should throw if instructions is not set`() {
        shouldThrow<IllegalArgumentException> {
            RawCoverageData {
                aClass = "class"
                branches(0, 0)
                lines(0, 0)
            }
        }
    }

    @Test
    fun `should throw if branches is not set`() {
        shouldThrow<IllegalArgumentException> {
            RawCoverageData {
                aClass = "class"
                instr(0, 0)
                lines(0, 0)
            }
        }
    }

    @Test
    fun `should throw if lines is not set`() {
        shouldThrow<IllegalArgumentException> {
            RawCoverageData {
                aClass = "class"
                instr(0, 0)
                branches(0, 0)
            }
        }
    }

    // cover the selected code
    @Test
    fun `should create new blank RawCoverageData`() {
        // GIVEN
        val data = RawCoverageData.newBlank {
            aClass = "class"
            instr(0, 0)
            branches(0, 0)
            lines(0, 0)
        }

        // THEN
        data shouldBeEqualToComparingFields RawCoverageData {
            aClass = "class"
            instr(0, 0)
            branches(0, 0)
            lines(0, 0)
        }
    }
}
