package io.github.surpsg.deltacoverage.diff

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.should
import io.kotest.matchers.string.endWith
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileDiffSourceTest {

    private val testProjectDir: File = Files.createTempDirectory("FileDiffSourceTest").toFile()

    @AfterAll
    fun cleanup() {
        testProjectDir.deleteRecursively()
    }

    @Test
    fun `pullDiff should throw when file doesn't exist`() {
        // setup
        val fileDiffSource = FileDiffSource("file-doesn't-exist")

        // run
        val exception = shouldThrow<RuntimeException> {
            fileDiffSource.pullDiff()
        }

        // assert
        exception.message should endWith("not a file or doesn't exist")
    }

    @Test
    fun `pullDiff should throw when specified path is dir`() {
        // setup
        val fileDiffSource = FileDiffSource(testProjectDir.newFolder().absolutePath)

        // run
        val exception = shouldThrow<RuntimeException> {
            fileDiffSource.pullDiff()
        }

        // assert
        exception.message should endWith("not a file or doesn't exist")
    }

    @Test
    fun `pullDiff should return file lines`() {
        // setup
        val expectedLines = listOf("1", "2", "3")
        val newFile = testProjectDir.newFile().apply {
            Files.write(toPath(), expectedLines, StandardOpenOption.APPEND)
        }

        val fileDiffSource = FileDiffSource(newFile.absolutePath)

        // run
        val diffLines = fileDiffSource.pullDiff()

        // assert
        diffLines shouldContainExactly expectedLines
    }

    private fun File.newFolder(): File {
        return resolve("${System.nanoTime()}").apply {
            mkdir()
        }
    }

    private fun File.newFile(): File {
        return resolve("${System.nanoTime()}").apply {
            createNewFile()
        }
    }
}

