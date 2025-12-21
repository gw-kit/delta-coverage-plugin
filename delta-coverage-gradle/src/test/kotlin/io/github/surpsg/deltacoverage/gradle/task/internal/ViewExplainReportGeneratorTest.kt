package io.github.surpsg.deltacoverage.gradle.task.internal

import io.github.surpsg.deltacoverage.gradle.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.CoverageEntity
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTaskConfigurer
import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.string.shouldContain
import org.gradle.api.internal.project.ProjectInternal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ViewExplainReportGeneratorTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var outputDir: File
    private lateinit var project: ProjectInternal
    private lateinit var config: DeltaCoverageConfiguration

    @BeforeEach
    fun setUp() {
        outputDir = tempDir.resolve("reports").apply { mkdirs() }
        project = testJavaProject {
            applyDeltaCoveragePlugin()
        }
        config = project.extensions.getByType(DeltaCoverageConfiguration::class.java).apply {
            diffSource.git.diffBase.set("refs/remotes/origin/main")
        }
    }

    @Nested
    inner class DefaultViewConfigurationTest {

        @Test
        fun `should generate report with default configuration`() {
            // GIVEN - use custom view name to avoid auto-discovery
            val viewName = "customView"
            config.view(viewName) {}
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val reportFile = outputDir.resolve("$viewName-explain-report.md")
            reportFile.shouldExist()

            val content = reportFile.readText()
            assertSoftly(content) {
                // Report structure
                shouldContain("# Delta Coverage Explain Report: `$viewName`")
                shouldContain("## Plugin Configuration")
                shouldContain("## Diff Configuration")
                shouldContain("## Reports Configuration")
                shouldContain("## '$viewName' View Details")
                shouldContain("## Environment")

                // Plugin configuration defaults
                shouldContain("- Coverage engine: JACOCO")
                shouldContain("- Reports output directory: ${outputDir.absolutePath}/")

                // Diff configuration
                shouldContain("- Source Git:")

                // View details defaults
                shouldContain("| Status | enabled |")
                shouldContain("| Origin | manual |")

                // Violation rules defaults
                shouldContain("| instruction | 0.0 | - | false |")
                shouldContain("| branch | 0.0 | - | false |")
                shouldContain("| line | 0.0 | - | false |")
                shouldContain("- Fail on violation: false")

                // Filters defaults
                shouldContain("**Filters:**")
                shouldContain("- Include classes: all")
                shouldContain("- Exclude classes: none")

                // Environment
                shouldContain("- Gradle version:")
                shouldContain("- Java version: ${System.getProperty("java.version")}")
                shouldContain("- Java vendor: ${System.getProperty("java.vendor")}")
            }
        }
    }

    @Nested
    inner class EmptyResolvedSourcesTest {

        @Test
        fun `should show none resolved for all source types when empty`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {}
            val generator = createGenerator(
                viewName,
                resolvedSources = ResolvedViewSources(
                    sources = emptySet(),
                    classes = emptySet(),
                    coverageBinaries = emptySet()
                )
            )

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            assertSoftly(content) {
                shouldContain("**Coverage Binary Files:**")
                shouldContain("**Source Directories:**")
                shouldContain("**Class Directories:**")
                shouldContain("- none resolved")
            }
        }
    }

    @Nested
    inner class PluginConfigurationSectionTest {

        @Test
        fun `should include custom coverage engine`() {
            // GIVEN
            val viewName = "test"
            config.apply {
                view(viewName) {}
                coverage.engine.set(CoverageEngine.INTELLIJ)
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "- Coverage engine: INTELLIJ"
        }

        @Test
        fun `should include auto-apply plugin setting when disabled`() {
            // GIVEN
            val viewName = "test"
            config.apply {
                view(viewName) {}
                coverage.autoApplyPlugin.set(false)
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "- Auto-apply coverage plugin: false"
        }
    }

    @Nested
    inner class DiffConfigurationSectionTest {

        @Test
        fun `should include diff source description for file source`() {
            // GIVEN
            val viewName = "test"
            val diffFile = tempDir.resolve("diff.patch").apply {
                writeText("test diff content")
            }
            config.apply {
                view(viewName) {}
                diffSource.file.set(diffFile.absolutePath)
                diffSource.git.diffBase.set("")
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "- Source File:"
        }
    }

    @Nested
    inner class ReportsConfigurationSectionTest {

        @Test
        fun `should include report types and full coverage setting`() {
            // GIVEN
            val viewName = "test"
            config.apply {
                view(viewName) {}
                reportConfiguration.apply {
                    html.set(true)
                    xml.set(false)
                    console.set(true)
                    markdown.set(false)
                    fullCoverageReport.set(true)
                }
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            assertSoftly(content) {
                shouldContain("| Report Type | Enabled |")
                shouldContain("| html | true |")
                shouldContain("| xml | false |")
                shouldContain("| console | true |")
                shouldContain("| markdown | false |")
                shouldContain("- Full coverage report: true")
            }
        }
    }

    @Nested
    inner class ViewDetailsSectionTest {

        @Test
        fun `should include view status as disabled when set`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) { it.enabled.set(false) }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "| Status | disabled |"
        }

        @Test
        fun `should show auto-created origin for aggregated view`() {
            // GIVEN
            val viewName = DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "| Origin | auto-created |"
        }

        @Test
        fun `should show discovered origin for auto-discovered view`() {
            // GIVEN
            val viewName = "test"
            config.reportViews.maybeCreate(viewName).autoDiscovered.set(true)
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "| Origin | discovered |"
        }

        @Test
        fun `should include associated projects section`() {
            // GIVEN
            val viewName = "test"
            config.apply {
                view(viewName) {}
                reportViews.getByName(viewName).associatedProjects.addAll(listOf(":app", ":lib"))
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            assertSoftly(content) {
                shouldContain("**Projects with this view:**")
                shouldContain("- :app")
                shouldContain("- :lib")
            }
        }
    }

    @Nested
    inner class CoverageBinaryFilesTest {

        @Test
        fun `should include coverage binary files table when files exist`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {}
            val binaryFile = tempDir.resolve("jacoco.exec").apply {
                writeBytes(ByteArray(2048))
            }
            val generator = createGenerator(
                viewName,
                resolvedSources = ResolvedViewSources(
                    sources = emptySet(),
                    classes = emptySet(),
                    coverageBinaries = setOf(binaryFile)
                )
            )

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            assertSoftly(content) {
                shouldContain("| File | Size | Exists |")
                shouldContain("| ${binaryFile.absolutePath} | 2 KB | true |")
            }
        }

        @Test
        fun `should show dash for size when file does not exist`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {}
            val nonExistentFile = tempDir.resolve("non-existent.exec")
            val generator = createGenerator(
                viewName,
                resolvedSources = ResolvedViewSources(
                    sources = emptySet(),
                    classes = emptySet(),
                    coverageBinaries = setOf(nonExistentFile)
                )
            )

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "| ${nonExistentFile.absolutePath} | - | false |"
        }

        @Test
        fun `should show bytes for small files`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {}
            val smallFile = tempDir.resolve("small.exec").apply {
                writeBytes(ByteArray(500))
            }
            val generator = createGenerator(
                viewName,
                resolvedSources = ResolvedViewSources(
                    sources = emptySet(),
                    classes = emptySet(),
                    coverageBinaries = setOf(smallFile)
                )
            )

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "| ${smallFile.absolutePath} | 500 B | true |"
        }
    }

    @Nested
    inner class SourceDirectoriesTest {

        @Test
        fun `should include source directories with file count`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {}
            val sourceDir = tempDir.resolve("src/main/java").apply {
                mkdirs()
                resolve("Main.java").writeText("class Main {}")
                resolve("Helper.java").writeText("class Helper {}")
            }
            val generator = createGenerator(
                viewName,
                resolvedSources = ResolvedViewSources(
                    sources = setOf(sourceDir),
                    classes = emptySet(),
                    coverageBinaries = emptySet()
                )
            )

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            assertSoftly(content) {
                shouldContain("**Source Directories:**")
                shouldContain("- ${sourceDir.absolutePath} (2 files)")
            }
        }
    }

    @Nested
    inner class ClassDirectoriesTest {

        @Test
        fun `should include class directories with class file count`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {}
            val classDir = tempDir.resolve("build/classes/java/main").apply {
                mkdirs()
                resolve("Main.class").writeBytes(ByteArray(100))
                resolve("Helper.class").writeBytes(ByteArray(100))
                resolve("readme.txt").writeText("Not a class file")
            }
            val generator = createGenerator(
                viewName,
                resolvedSources = ResolvedViewSources(
                    sources = emptySet(),
                    classes = setOf(classDir),
                    coverageBinaries = emptySet()
                )
            )

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            assertSoftly(content) {
                shouldContain("**Class Directories:**")
                shouldContain("- ${classDir.absolutePath} (2 files)")
            }
        }
    }

    @Nested
    inner class ViolationRulesTest {

        @Test
        fun `should include violation rules table with thresholds`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {
                it.violationRules.failIfCoverageLessThan(0.8)
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            assertSoftly(content) {
                shouldContain("**Violation Rules:**")
                shouldContain("| Metric | Min threshold | Entity count threshold | Enabled |")
                shouldContain("| instruction | 0.8 | - | true |")
                shouldContain("| branch | 0.8 | - | true |")
                shouldContain("| line | 0.8 | - | true |")
            }
        }

        @Test
        fun `should include entity count threshold when set`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {
                it.violationRules.rule(CoverageEntity.LINE) { rule ->
                    rule.minCoverageRatio.set(0.75)
                    rule.entityCountThreshold.set(10)
                }
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "| line | 0.75 | 10 | true |"
        }

        @Test
        fun `should include fail on violation setting when enabled`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {
                it.violationRules.failOnViolation.set(true)
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "- Fail on violation: true"
        }
    }

    @Nested
    inner class FiltersTest {

        @Test
        fun `should include match classes filter when set`() {
            // GIVEN
            val viewName = "test"
            config.view(viewName) {
                it.matchClasses.addAll(listOf("com.example.*", "org.test.*"))
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "- Include classes: `com.example.*`, `org.test.*`"
        }

        @Test
        fun `should include exclude classes when set`() {
            // GIVEN
            val viewName = "test"
            config.apply {
                view(viewName) {}
                excludeClasses.addAll(listOf("*Test*", "*Mock*"))
            }
            val generator = createGenerator(viewName)

            // WHEN
            generator.generateReport()

            // THEN
            val content = readReportContent(viewName)
            content shouldContain "- Exclude classes: `*Test*`, `*Mock*`"
        }
    }

    private fun readReportContent(viewName: String): String =
        outputDir.resolve("$viewName-explain-report.md").readText()

    private fun createGenerator(
        viewName: String,
        resolvedSources: ResolvedViewSources = ResolvedViewSources(
            sources = emptySet(),
            classes = emptySet(),
            coverageBinaries = emptySet()
        )
    ): ViewExplainReportGenerator = ViewExplainReportGenerator(
        view = viewName,
        outputDir = outputDir,
        gradleConfig = config,
        rootProject = project,
        resolvedSources = resolvedSources
    )
}