package io.github.gwkit.testimpact.gradle.test.listener

import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Gradle TestListener that collects test class names during test execution.
 * Writes collected test classes to a file when test suite finishes.
 */
internal class TestEventsCollector(
    private val outputFile: File
) : TestListener {

    private val testClasses = ConcurrentHashMap.newKeySet<String>()

    override fun beforeSuite(suite: TestDescriptor) = Unit

    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
        // Write collected test classes to file when root suite finishes
        if (suite.parent == null && testClasses.isNotEmpty()) {
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(testClasses.sorted().joinToString("\n"))
        }
    }

    override fun beforeTest(testDescriptor: TestDescriptor) = Unit

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        testDescriptor.className?.let { testClasses.add(it) }
    }
}
