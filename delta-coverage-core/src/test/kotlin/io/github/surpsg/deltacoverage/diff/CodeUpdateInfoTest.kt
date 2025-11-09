package io.github.surpsg.deltacoverage.diff

import io.github.surpsg.deltacoverage.diff.parse.ClassFile
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class ClassModificationsTest {

    @ParameterizedTest
    @MethodSource("lineIsModifiedTestCases")
    fun `isLineModified should return true when line is modified`(line: Int, modifiedLines: Set<Int>) {
        // setup
        val classModifications = ClassModifications(modifiedLines)

        // assert
        classModifications.isLineModified(line) shouldBe true
    }

    @ParameterizedTest
    @MethodSource("lineIsNotModifiedTestCases")
    fun `isLineModified should return false when line is not modified`(line: Int, modifiedLines: Set<Int>) {
        // setup
        val classModifications = ClassModifications(modifiedLines)

        // assert
        classModifications.isLineModified(line) shouldBe false
    }

    companion object {
        @JvmStatic
        fun lineIsModifiedTestCases() = listOf(
            arguments(1, setOf(1, 2, 3))
        )

        @JvmStatic
        fun lineIsNotModifiedTestCases() = listOf(
            arguments(1, setOf<Int>()),
            arguments(0, setOf(1, 2)),
            arguments(-1, setOf(1, 2)),
            arguments(3, setOf(1, 2))
        )
    }
}

class CodeUpdateInfoTest {

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 10, 15, 20, 25, 30, 100, 200, -1])
    fun `getClassModifications should return empty ClassModifications when no such info`(lineNumber: Int) {
        // setup
        val codeUpdateInfo = CodeUpdateInfo(
            mapOf("com/package/Class.java" to setOf(12))
        )

        // run
        val classModifications = codeUpdateInfo.getClassModifications(
            ClassFile("UnknownClass.java", "com/package/UnknownClass")
        )

        // assert
        classModifications.isLineModified(lineNumber) shouldBe false
    }

    @Test
    fun `isInfoExists should return true when modifications info exists for class`() {
        // setup
        val codeUpdateInfo = CodeUpdateInfo(
            mapOf("module/src/main/java/com/package/Class.java" to setOf(1, 2, 3))
        )

        // run
        val infoExists = codeUpdateInfo.isInfoExists(
            ClassFile("Class.java", "com/package/Class")
        )

        // assert
        infoExists shouldBe true
    }

    @Test
    fun `getClassModifications should return modifications for class when there is similar class name exists`() {
        // setup
        val expectedLineNumber = 1
        val requestedLineNumber1 = 2
        val requestedLineNumber2 = 3
        val codeUpdateInfo = CodeUpdateInfo(
            mapOf(
                "src/com/package/ClassSuffix.java" to setOf(requestedLineNumber1),
                "src/com/package/Class.java" to setOf(expectedLineNumber),
                "src/com/package/PrefixClass.java" to setOf(requestedLineNumber2)
            )
        )

        // run
        val modifications = codeUpdateInfo.getClassModifications(
            ClassFile("Class.java", "com/package/Class")
        )

        // assert
        modifications.isLineModified(2) shouldBe false
        modifications.isLineModified(3) shouldBe false
        modifications.isLineModified(1) shouldBe true
    }

    @Test
    fun `isInfoExists should return false when modifications info doesn't exist for class`() {
        data class TestCase(
            val classSourceFile: String,
            val classNameToCheck: String,
            val mapOfModifiedLines: Map<String, Set<Int>>
        )

        val testCases = listOf(
            TestCase(
                "OtherClass.java",
                "com/package/OtherClass",
                mapOf("src/java/com/package/Class.java" to setOf(1, 2, 3))
            ),
            TestCase(
                "Class.java",
                "com/package/Class",
                mapOf("src/java/com/package/Class.java" to setOf())
            ),
            TestCase(
                "Class.java",
                "com/package/Class",
                mapOf()
            )
        )

        testCases.forEach { (classSourceFile, classNameToCheck, mapOfModifiedLines) ->
            // setup
            val codeUpdateInfo = CodeUpdateInfo(mapOfModifiedLines)

            // run
            val infoExists = codeUpdateInfo.isInfoExists(
                ClassFile(classSourceFile, classNameToCheck)
            )

            // assert
            infoExists shouldBe false
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "io.github.deltacoverage.analyzable.AnalyzableReportKt, AnalyzableReport.kt, 1",
            "io.github.deltacoverage.analyzable.AnalyzableReport, AnalyzableReport.kt, 1",
            "io.github.deltacoverage.analyzable.DeltaCoverageAnalyzableReport, DeltaCoverageAnalyzableReport.kt, 2",
            "io.github.deltacoverage.analyzable.FullCoverageAnalyzableReport, FullCoverageAnalyzableReport.kt, 3",
        ]
    )
    fun `getClassModifications should return correct modifications for class when there are class with similar names`(
        className: String,
        sourceFile: String,
        expectedModifiedLine: Int,
    ) {
        // GIVEN
        val codeUpdateInfo = CodeUpdateInfo(
            linkedMapOf(
                // another package, same file name
                "b/src/main/kotlin/io/github/another/AnalyzableReport.kt" to setOf(4),

                // common file name suffix
                "b/src/main/kotlin/io/github/deltacoverage/analyzable/FullCoverageAnalyzableReport.kt" to setOf(3),
                // common file name suffix
                "b/src/main/kotlin/io/github/deltacoverage/analyzable/DeltaCoverageAnalyzableReport.kt" to setOf(2),
                // common file name suffix
                "b/src/main/kotlin/io/github/deltacoverage/analyzable/AnalyzableReport.kt" to setOf(1),
            )
        )

        // WHEN
        val actualModifiedLines: ClassModifications = codeUpdateInfo.getClassModifications(
            ClassFile(sourceFile, className)
        )

        // THEN
        actualModifiedLines.isLineModified(expectedModifiedLine) shouldBe true
    }
}
