package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.data.instructions.ClassInstructions
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import org.junit.jupiter.api.Test

class ClassDataCopingContextTest {

    @Test
    fun `copyClassData should copy class data from source to destination`() {
        // GIVEN
        val expectedSource = "MyClass.java"
        val expectedClassName = "com.example.MyClass"
        val sourceProjectData = ProjectData().apply {
            getOrCreateClassData(expectedClassName).apply {
                source = expectedSource
                instructions[expectedClassName] = ClassInstructions()
            }
        }
        val copyToProjectData = ProjectData()

        // WHEN
        val result: ClassData = ClassDataCopingContext(
            className = expectedClassName,
            sourceProjectData = sourceProjectData,
            copyToProjectData = copyToProjectData
        ).copyClassData()

        // THEN
        assertSoftly(result) {
            source shouldBeEqual expectedSource
            name shouldBeEqual expectedClassName
        }

        // AND THEN
        copyToProjectData.instructions.shouldContainKey(expectedClassName)
    }

    @Test
    fun `copyClassData should copy class data without instructions when there is no instructions for class`() {
        // GIVEN
        val expectedSource = "MyClass.java"
        val expectedClassName = "com.example.MyClass"
        val sourceProjectData = ProjectData().apply {
            getOrCreateClassData(expectedClassName).source = expectedSource
        }
        val copyToProjectData = ProjectData()

        // WHEN
        val result: ClassData = ClassDataCopingContext(
            className = expectedClassName,
            sourceProjectData = sourceProjectData,
            copyToProjectData = copyToProjectData
        ).copyClassData()

        // THEN
        assertSoftly(result) {
            source shouldBeEqual expectedSource
            name shouldBeEqual expectedClassName
        }

        // AND THEN
        copyToProjectData.instructions.shouldBeEmpty()
    }
}
