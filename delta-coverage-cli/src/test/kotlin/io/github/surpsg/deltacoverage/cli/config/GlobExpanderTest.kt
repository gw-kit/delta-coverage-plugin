package io.github.surpsg.deltacoverage.cli.config

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class GlobExpanderTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should return file when explicit path exists`() {
        // given
        val testFile = tempDir.resolve("test.txt").toFile().apply { createNewFile() }

        // when
        val result = GlobExpander.expandGlob(testFile.absolutePath, tempDir.toFile())

        // then
        result shouldHaveSize 1
        result.first().absolutePath shouldBe testFile.absolutePath
    }

    @Test
    fun `should return empty list when explicit path does not exist`() {
        // given
        val nonExistentPath = tempDir.resolve("non-existent.txt").toString()

        // when
        val result = GlobExpander.expandGlob(nonExistentPath, tempDir.toFile())

        // then
        result.shouldBeEmpty()
    }

    @Test
    fun `should expand simple glob pattern`() {
        // given
        tempDir.resolve("file1.txt").toFile().createNewFile()
        tempDir.resolve("file2.txt").toFile().createNewFile()
        tempDir.resolve("file3.log").toFile().createNewFile()

        // when
        val result = GlobExpander.expandGlob("*.txt", tempDir.toFile())

        // then
        result shouldHaveSize 2
        result.map { it.name } shouldContainExactlyInAnyOrder listOf("file1.txt", "file2.txt")
    }

    @Test
    fun `should expand recursive glob pattern`() {
        // given
        val subDir = tempDir.resolve("subdir").toFile().apply { mkdir() }
        val deepSubDir = subDir.toPath().resolve("deep").toFile().apply { mkdir() }

        File(subDir, "sub.exec").createNewFile()
        File(deepSubDir, "deep.exec").createNewFile()

        // when
        val result = GlobExpander.expandGlob("**/*.exec", tempDir.toFile())

        // then - note: ** matches one or more directories, so root level files are not matched
        result shouldHaveSize 2
        result.map { it.name } shouldContainExactlyInAnyOrder listOf("sub.exec", "deep.exec")
    }

    @Test
    fun `should expand multiple patterns`() {
        // given
        tempDir.resolve("test1.txt").toFile().createNewFile()
        tempDir.resolve("test2.txt").toFile().createNewFile()
        tempDir.resolve("test.log").toFile().createNewFile()

        // when
        val result = GlobExpander.expandGlobs(listOf("*.txt", "*.log"), tempDir.toFile())

        // then
        result shouldHaveSize 3
        result.map { it.name } shouldContainExactlyInAnyOrder listOf("test1.txt", "test2.txt", "test.log")
    }

    @Test
    fun `should deduplicate results from multiple patterns`() {
        // given
        tempDir.resolve("test.txt").toFile().createNewFile()

        // when
        val result = GlobExpander.expandGlobs(listOf("*.txt", "test.*"), tempDir.toFile())

        // then
        result shouldHaveSize 1
        result.first().name shouldBe "test.txt"
    }

    @Test
    fun `should handle mixed explicit paths and glob patterns`() {
        // given
        val explicitFile = tempDir.resolve("explicit.txt").toFile().apply { createNewFile() }
        tempDir.resolve("glob1.log").toFile().createNewFile()
        tempDir.resolve("glob2.log").toFile().createNewFile()

        // when
        val result = GlobExpander.expandGlobs(
            listOf(explicitFile.absolutePath, "*.log"),
            tempDir.toFile()
        )

        // then
        result shouldHaveSize 3
        result.map { it.name } shouldContainExactlyInAnyOrder listOf("explicit.txt", "glob1.log", "glob2.log")
    }
}
