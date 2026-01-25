package io.github.surpsg.deltacoverage.cli.config

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

class GlobExpanderTest {

    private lateinit var fileSystem: FileSystem
    private lateinit var tempDir: Path
    private lateinit var globExpander: GlobExpander

    @BeforeEach
    fun setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix())
        tempDir = fileSystem.getPath("/tmp/test")
        Files.createDirectories(tempDir)
        globExpander = GlobExpander(fileSystem, tempDir)
    }

    @AfterEach
    fun tearDown() {
        fileSystem.close()
    }

    @Test
    fun `should return file when explicit path exists`() {
        // given
        val testFile = tempDir.resolve("test.txt")
        Files.createFile(testFile)

        // when
        val result = globExpander.expandGlob(testFile.toString())

        // then
        result shouldHaveSize 1
        result.first().toAbsolutePath().toString() shouldBe testFile.toAbsolutePath().toString()
    }

    @Test
    fun `should return empty list when explicit path does not exist`() {
        // given
        val nonExistentPath = tempDir.resolve("non-existent.txt").toString()

        // when
        val result = globExpander.expandGlob(nonExistentPath)

        // then
        result.shouldBeEmpty()
    }

    @Test
    fun `should expand simple glob pattern`() {
        // given
        Files.createFile(tempDir.resolve("file1.txt"))
        Files.createFile(tempDir.resolve("file2.txt"))
        Files.createFile(tempDir.resolve("file3.log"))

        // when
        val result = globExpander.expandGlob("*.txt")

        // then
        result shouldHaveSize 2
        result.map { it.fileName.toString() } shouldContainExactlyInAnyOrder listOf("file1.txt", "file2.txt")
    }

    @Test
    fun `should expand recursive glob pattern`() {
        // given
        val subDir = tempDir.resolve("subdir")
        Files.createDirectories(subDir)
        val deepSubDir = subDir.resolve("deep")
        Files.createDirectories(deepSubDir)

        Files.createFile(subDir.resolve("sub.exec"))
        Files.createFile(deepSubDir.resolve("deep.exec"))

        // when
        val result = globExpander.expandGlob("**/*.exec")

        // then - note: ** matches one or more directories, so root level files are not matched
        result shouldHaveSize 2
        result.map { it.fileName.toString() } shouldContainExactlyInAnyOrder listOf("sub.exec", "deep.exec")
    }

    @Test
    fun `should expand multiple patterns`() {
        // given
        Files.createFile(tempDir.resolve("test1.txt"))
        Files.createFile(tempDir.resolve("test2.txt"))
        Files.createFile(tempDir.resolve("test.log"))

        // when
        val result = globExpander.expandGlobs(listOf("*.txt", "*.log"))

        // then
        result shouldHaveSize 3
        result.map { it.fileName.toString() } shouldContainExactlyInAnyOrder listOf("test1.txt", "test2.txt", "test.log")
    }

    @Test
    fun `should deduplicate results from multiple patterns`() {
        // given
        Files.createFile(tempDir.resolve("test.txt"))

        // when
        val result = globExpander.expandGlobs(listOf("*.txt", "test.*"))

        // then
        result shouldHaveSize 1
        result.first().fileName.toString() shouldBe "test.txt"
    }

    @Test
    fun `should handle mixed explicit paths and glob patterns`() {
        // given
        val explicitFile = tempDir.resolve("explicit.txt")
        Files.createFile(explicitFile)
        Files.createFile(tempDir.resolve("glob1.log"))
        Files.createFile(tempDir.resolve("glob2.log"))

        // when
        val result = globExpander.expandGlobs(
            listOf(explicitFile.toString(), "*.log")
        )

        // then
        result shouldHaveSize 3
        result.map { it.fileName.toString() } shouldContainExactlyInAnyOrder listOf("explicit.txt", "glob1.log", "glob2.log")
    }
}
