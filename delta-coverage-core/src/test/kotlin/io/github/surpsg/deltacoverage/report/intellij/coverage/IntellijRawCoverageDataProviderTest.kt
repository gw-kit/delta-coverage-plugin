package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.data.instructions.ClassInstructions
import com.intellij.rt.coverage.data.instructions.LineInstructions
import io.github.surpsg.deltacoverage.report.textual.RawCoverageData
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


class IntellijRawCoverageDataProviderTest {

    @Test
    fun `should return empty list when no data`() {
        // GIVEN
        val provider = IntellijRawCoverageDataProvider(ProjectData())

        // WHEN
        val actualData = provider.obtainData()

        // THEN
        actualData.shouldBeEmpty()
    }

    @Test
    fun `should return list with single class coverage`() {
        // GIVEN
        val className = "com.example.Class1"
        val sourceName = "Class1.java"
        val provider = IntellijRawCoverageDataProvider(
            ProjectData().apply {
                instructions[className] = ClassInstructions(
                    arrayOf(
                        LineInstructions().apply {
                            instructions = 3
                        },
                        LineInstructions().apply {
                            instructions = 5
                        }
                    )
                )

                getOrCreateClassData(className).apply {
                    source = sourceName

                    setLines(
                        arrayOf(LineData(1, "").apply { hits = 1 })
                    )
                }
            }
        )

        // WHEN
        val actualData: List<RawCoverageData> = provider.obtainData()

        // THEN
        assertSoftly(actualData) {
            size shouldBe 1
            first() shouldBeEqualToComparingFields RawCoverageData {
                aClass = className
                branches(0, 0)
                lines(1, 1)
                instr(5, 5)
            }
        }
    }
}
