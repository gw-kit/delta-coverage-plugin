package io.github.surpsg.deltacoverage.diff.parse

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ClassFileTest {

    @Test
    fun `ClassFile path should return path with slash when class has no package`() {
        // setup
        val classFile = ClassFile("Class1.java", "Class1")

        // run // assert
        classFile.path shouldBe "/Class1.java"
    }

    @Test
    fun `ClassFile path should return relative path when class has package`() {
        // setup
        val classFile = ClassFile("Class1.java", "com/java/test/Class1")

        // run // assert
        classFile.path shouldBe "com/java/test/Class1.java"
    }

    @Test
    fun `ClassFile path should return relative path when class name in fully qualified form`() {
        // setup
        val classFile = ClassFile("Class1.java", "com.java.test.Class1")

        // run // assert
        classFile.path shouldBe "com/java/test/Class1.java"
    }
}
