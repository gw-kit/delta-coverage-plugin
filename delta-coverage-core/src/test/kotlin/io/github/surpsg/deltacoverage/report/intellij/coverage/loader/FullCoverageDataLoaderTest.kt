package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import io.github.surpsg.deltacoverage.report.intellij.coverage.IntellijSourceInputs
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

class FullCoverageDataLoaderTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `load should return empty ProjectData when no binary reports provided`() {
        // GIVEN
        val loader = FullCoverageDataLoader()
        val intellijSourceInputs = IntellijSourceInputs(
            allClasses = emptySet(),
            classesRoots = emptyList(),
            excludeClasses = emptySet(),
            sourcesFiles = emptyList()
        )

        // WHEN
        val result = loader.load(emptyList(), intellijSourceInputs)

        // THEN
        result.classes.shouldBeEmpty()
        result.instructions.shouldBeEmpty()
    }

    @Test
    fun `load should filter out excluded classes`() {
        // GIVEN
        val loader = FullCoverageDataLoader()
        val classesDir = tempDir.resolve("classes").createDirectories()
        classesDir.resolve("KeepThis.class").createFile()
        classesDir.resolve("ExcludeThis.class").createFile()

        val intellijSourceInputs = IntellijSourceInputs(
            allClasses = emptySet(),
            classesRoots = listOf(classesDir.toFile()),
            excludeClasses = setOf(".*ExcludeThis\\.class$"),
            sourcesFiles = emptyList()
        )

        // WHEN
        val result = loader.load(emptyList(), intellijSourceInputs)

        // THEN
        // The result should not contain excluded class
        result.classes.keys.any { it.contains("ExcludeThis") } shouldBe false
    }
}
