package io.github.gwkit.testimpact.gradle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.gwkit.gradleprobe.RestorableFile
import io.github.gwkit.gradleprobe.gradlerunner.runTask
import io.github.gwkit.gradleprobe.junit.GradlePluginTest
import io.github.gwkit.gradleprobe.junit.GradleRunnerInstance
import io.github.gwkit.gradleprobe.junit.ProjectFile
import io.github.gwkit.gradleprobe.junit.RootProjectDir
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@Suppress("UNCHECKED_CAST")
@GradlePluginTest("single-module-test-project")
class TestMappingFunctionalTest {

    @RootProjectDir
    lateinit var rootProjectDir: File

    @ProjectFile("build.gradle.kts")
    lateinit var buildFile: RestorableFile

    @GradleRunnerInstance
    lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun beforeEach() {
        buildFile.restoreOriginContent()
    }

    @Test
    fun `flamegraph should be created when enabled`() {
        // GIVEN
        buildFile.file.appendText(
            """
            testImpact {
                enabled = true
                includePackages.add("com.java.test")
                reports {
                    html.set(true)
                    flamegraph.set(true)
                }
            }
        """.trimIndent()
        )

        // WHEN
        gradleRunner.runTask("test", "analyzeTestMapping")
            .apply {
                println(output)
            }

        // THEN
        val reportDir = rootProjectDir.resolve("build/reports/test-impact")

        // TODO
//        reportDir.resolve("test-mapping.html").shouldBeAFile()

        val flamegraphFile = reportDir.resolve("flamegraph.html")
        flamegraphFile.shouldBeAFile()
        assertSoftly(flamegraphFile.readText()) {
            shouldContain("canvas")
            shouldContain("async-profiler")
        }
    }
}
