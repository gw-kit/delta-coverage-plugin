package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.FileSystem
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.div

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ClassesDirLoaderTest {

    private val fileSystem: FileSystem = Jimfs.newFileSystem(Configuration.unix())
    private val tempDir = fileSystem.getPath("/").createDirectories()

    @Test
    fun `traverseClasses should return empty sequence when root directory does not exist`() {
        // GIVEN
        val nonExistentDir = tempDir / "non-existent"
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(nonExistentDir).toList()

        // THEN
        result.shouldBeEmpty()
    }

    @Test
    fun `traverseClasses should return empty sequence when directory is empty`() {
        // GIVEN
        val emptyDir = (tempDir / "empty").createDirectories()
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(emptyDir).toList()

        // THEN
        result.shouldBeEmpty()
    }

    @Test
    fun `traverseClasses should find single class file in root directory`() {
        // GIVEN
        val classFile = tempDir.resolve("MyClass.class").createFile()
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(1)
            .single()
            .shouldBeEqual(JvmClassDesc("MyClass", classFile))
    }

    @Test
    fun `traverseClasses should resolve class names with package structure`() {
        // GIVEN
        val packageDir = tempDir.resolve("com/example/project").createDirectories()
        val classFile = packageDir.resolve("MyClass.class").createFile()
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(1)
            .single()
            .shouldBeEqual(JvmClassDesc("com.example.project.MyClass", classFile))
    }

    @Test
    fun `traverseClasses should find multiple class files in nested directories`() {
        // GIVEN
        val dir1 = tempDir.resolve("com/example").createDirectories()
        val dir2 = tempDir.resolve("org/test").createDirectories()
        dir1.resolve("Class1.class").createFile()
        dir1.resolve("Class2.class").createFile()
        dir2.resolve("Class3.class").createFile()
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(3)
            .map { it.className }
            .shouldContainExactly(
                "com.example.Class1",
                "com.example.Class2",
                "org.test.Class3"
            )
    }

    @Test
    fun `traverseClasses should sort classes alphabetically by name within same directory`() {
        // GIVEN
        val packageDir = tempDir.resolve("com/example").createDirectories()
        packageDir.resolve("Zebra.class").createFile()
        packageDir.resolve("Alpha.class").createFile()
        packageDir.resolve("Beta.class").createFile()
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result.map { it.className }.shouldContainExactly(
            "com.example.Alpha",
            "com.example.Beta",
            "com.example.Zebra"
        )
    }

    @Test
    fun `traverseClasses should ignore non-class files`() {
        // GIVEN
        val packageDir = tempDir.resolve("com/example").createDirectories()
        packageDir.resolve("readme.txt").createFile()
        packageDir.resolve("config.xml").createFile()
        packageDir.resolve("data.json").createFile()
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result.shouldBeEmpty()
    }

    @Test
    fun `traverseClasses should filter by include list when specified`() {
        // GIVEN
        val packageDir = tempDir.resolve("com/example").createDirectories()
        val includedFile = packageDir.resolve("Included.class").createFile()
        packageDir.resolve("Excluded.class").createFile()
        val loader = ClassesDirLoader(setOf(includedFile), emptySet())

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(1)
            .single()
            .shouldBeEqual(JvmClassDesc("com.example.Included", includedFile))
    }

    @Test
    fun `traverseClasses should include all files when include list is empty`() {
        // GIVEN
        val packageDir = tempDir.resolve("com/example").createDirectories()
        packageDir.resolve("Class1.class").createFile()
        packageDir.resolve("Class2.class").createFile()
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result.shouldHaveSize(2)
    }

    @Test
    fun `traverseClasses should exclude files matching exclude patterns`() {
        // GIVEN
        val packageDir = tempDir.resolve("com/example").createDirectories()
        val keepFile = packageDir.resolve("KeepThis.class").createFile()
        packageDir.resolve("ExcludeThis.class").createFile()
        val excludePattern = ".*ExcludeThis\\.class$"
        val loader = ClassesDirLoader(emptySet(), setOf(excludePattern))

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(1)
            .single()
            .shouldBeEqual(JvmClassDesc("com.example.KeepThis", keepFile))
    }

    @Test
    fun `traverseClasses should exclude directories matching exclude patterns`() {
        // GIVEN
        val keepDir = tempDir.resolve("com/keep").createDirectories()
        val excludeDir = tempDir.resolve("com/exclude").createDirectories()
        val keepFile = keepDir.resolve("Class1.class").createFile()
        excludeDir.resolve("Class2.class").createFile()
        val excludePattern = ".*/exclude/.*"
        val loader = ClassesDirLoader(emptySet(), setOf(excludePattern))

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(1)
            .single()
            .shouldBeEqual(JvmClassDesc("com.keep.Class1", keepFile))
    }

    @Test
    fun `traverseClasses should exclude entire directory when pattern matches directory`() {
        // GIVEN
        val includeDir = tempDir.resolve("src/main/java").createDirectories()
        val excludeDir = tempDir.resolve("src/test/java").createDirectories()
        val mainFile = includeDir.resolve("MainClass.class").createFile()
        excludeDir.resolve("TestClass.class").createFile()
        val excludePattern = ".*/test/.*"
        val loader = ClassesDirLoader(emptySet(), setOf(excludePattern))

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(1)
            .single()
            .shouldBeEqual(JvmClassDesc("src.main.java.MainClass", mainFile))
    }

    @Test
    fun `traverseClasses should apply multiple exclude patterns`() {
        // GIVEN
        val packageDir = tempDir.resolve("com/example").createDirectories()
        val keepFile = packageDir.resolve("Keep.class").createFile()
        packageDir.resolve("Test.class").createFile()
        packageDir.resolve("Generated.class").createFile()
        val excludePatterns = setOf(".*Test\\.class$", ".*Generated\\.class$")
        val loader = ClassesDirLoader(emptySet(), excludePatterns)

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(1)
            .single()
            .shouldBeEqual(JvmClassDesc("com.example.Keep", keepFile))
    }

    @Test
    fun `traverseClasses should combine include and exclude filters`() {
        // GIVEN
        val packageDir = tempDir.resolve("com/example").createDirectories()
        val includedFile = packageDir.resolve("Included.class").createFile()
        val excludedFile = packageDir.resolve("Test.class").createFile()
        packageDir.resolve("Other.class").createFile()
        val excludePattern = ".*Test\\.class$"
        val loader = ClassesDirLoader(setOf(includedFile, excludedFile), setOf(excludePattern))

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(1)
            .single()
            .shouldBeEqual(JvmClassDesc("com.example.Included", includedFile))
    }

    @Test
    fun `traverseClasses should handle deeply nested package structure`() {
        // GIVEN
        val deepDir = tempDir.resolve("com/company/project/module/submodule/package1/package2")
            .createDirectories()
        val classFile = deepDir.resolve("DeepClass.class").createFile()
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(1)
            .single()
            .shouldBeEqual(JvmClassDesc("com.company.project.module.submodule.package1.package2.DeepClass", classFile))
    }

    @Test
    fun `traverseClasses should handle inner classes`() {
        // GIVEN
        val packageDir = tempDir.resolve("com/example").createDirectories()
        packageDir.resolve("Outer.class").createFile()
        packageDir.resolve("Outer\$Inner.class").createFile()
        val loader = ClassesDirLoader(emptySet(), emptySet())

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result
            .shouldHaveSize(2)
            .map { it.className }
            .shouldContainExactly(
                "com.example.Outer",
                "com.example.Outer\$Inner"
            )
    }

    @Test
    fun `traverseClasses should exclude root directory when it matches exclude pattern`() {
        // GIVEN
        tempDir.resolve("Class1.class").createFile()
        val excludePattern = tempDir.absolutePathString()
        val loader = ClassesDirLoader(emptySet(), setOf(excludePattern))

        // WHEN
        val result = loader.traverseClasses(tempDir).toList()

        // THEN
        result.shouldBeEmpty()
    }
}
