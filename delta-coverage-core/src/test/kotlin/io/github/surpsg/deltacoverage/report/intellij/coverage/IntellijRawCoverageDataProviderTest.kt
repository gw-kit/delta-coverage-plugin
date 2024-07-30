package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.JumpsAndSwitches
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
        val instrCovered = 3
        val instrUncovered = 5
        val provider = IntellijRawCoverageDataProvider(
            ProjectData().apply {
                instructions[className] = ClassInstructions(
                    arrayOf(
                        LineInstructions().apply { instructions = instrCovered },
                        LineInstructions().apply { instructions = instrUncovered }
                    )
                )

                getOrCreateClassData(className).apply {
                    source = sourceName

                    setLines(
                        arrayOf(
                            LineData(0, "").apply { touch() },
                            LineData(1, "").apply {
                                setJumpsAndSwitches(
                                    JumpsAndSwitches().apply {
                                        addSwitch(0, IntArray(1) { 0 })
                                        fillArrays()
                                    }
                                )
                            },
                        )
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
                branches(0, 1)
                lines(1, 2)
                instr(instrCovered, instrCovered + instrUncovered)
            }
        }
    }

    @Test
    fun `should return list with single class and no coverage`() {
        // GIVEN
        val className = "com.example.Class2"
        val sourceName = "Class2.java"
        val provider = IntellijRawCoverageDataProvider(
            ProjectData().apply {
                instructions[className] = ClassInstructions(emptyArray())

                getOrCreateClassData(className).apply {
                    source = sourceName
                    setLines(arrayOf(LineData(1, "")))
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
                lines(0, 1)
                instr(0, 0)
            }
        }
    }
}
