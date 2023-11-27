package io.github.surpsg.deltacoverage.gradle.sources

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.lookup.JacocoPluginSourcesLookup.Companion.JACOCO_REPORT_TASK
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testing.jacoco.tasks.JacocoReportBase
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KMutableProperty1

internal class SourcesResolverTest {

    private val project: Project = ProjectBuilder.builder().build()

    private val sourcesResolver = SourcesResolver()

    @ParameterizedTest
    @MethodSource("delta coverage source test parameters")
    fun `should return delta coverage configured files`(
        sourceType: SourceType,
        deltaConfigSetter: KMutableProperty1<DeltaCoverageConfiguration, FileCollection>
    ) {
        // GIVEN
        val expectedFile = "expected/path/$sourceType"
        val context: SourcesResolver.Context = project.sourceContext(sourceType) {
            coverageEngine = CoverageEngine.JACOCO
            deltaConfigSetter.set(this, project.files(expectedFile))
        }

        // WHEN
        val resolvedFiles: FileCollection = sourcesResolver.resolve(context)

        // THEN
        assertSoftly(resolvedFiles) {
            shouldHaveSize(1)
            first().path shouldEndWith expectedFile
        }
    }

    @ParameterizedTest
    @MethodSource("delta coverage source test parameters")
    fun `should throw if source is empty and delta-cov sources are empty`(
        sourceType: SourceType,
        deltaConfigSetter: KMutableProperty1<DeltaCoverageConfiguration, FileCollection>
    ) {
        // GIVEN
        val emptyFiles = project.files()
        val context: SourcesResolver.Context = project.sourceContext(sourceType) {
            coverageEngine = CoverageEngine.JACOCO
            deltaConfigSetter.set(this, emptyFiles)
        }

        // WHEN // THEN
        val actualException = shouldThrow<IllegalStateException> {
            sourcesResolver.resolve(context)
        }

        // AND THEN
        assertSoftly(actualException.message) {
            shouldContain(sourceType.sourceConfigurationPath)
            shouldContain("file collection is empty")
        }
    }

    @ParameterizedTest
    @EnumSource(SourceType::class)
    fun `should throw if source is empty and delta-cov sources are not set and jacoco is not applied`(
        sourceType: SourceType
    ) {
        // GIVEN
        val context: SourcesResolver.Context = project.sourceContext(sourceType) {
            coverageEngine = CoverageEngine.JACOCO
        }

        // WHEN // THEN
        val actualException = shouldThrow<IllegalStateException> {
            sourcesResolver.resolve(context)
        }

        // AND THEN
        assertSoftly(actualException.message) {
            shouldContain(sourceType.sourceConfigurationPath)
            shouldContain("is not configured")
        }
    }

    @ParameterizedTest
    @EnumSource(SourceType::class)
    fun `should throw if source is empty and delta-cov sources are not set and jacoco files is empty`(
        sourceType: SourceType
    ) {
        // GIVEN
        project.applyJacocoPlugin {
            sourceDirectories.setFrom(project.files())
            classDirectories.setFrom(project.files())
            executionData.setFrom(project.files())
        }
        val context: SourcesResolver.Context = project.sourceContext(sourceType) {
            coverageEngine = CoverageEngine.JACOCO
        }

        // WHEN // THEN
        val actualException = shouldThrow<IllegalStateException> {
            sourcesResolver.resolve(context)
        }

        // AND THEN
        assertSoftly(actualException.message) {
            shouldContain(sourceType.sourceConfigurationPath)
            shouldContain("is not configured.")
        }
    }

    private fun Project.applyJacocoPlugin(
        customize: JacocoReportBase.() -> Unit
    ) {
        pluginManager.apply {
            apply("java")
            apply("jacoco")
        }
        tasks.findByPath(JACOCO_REPORT_TASK)
            .let { it as JacocoReportBase }
            .apply(customize)
    }

    private fun Project.sourceContext(
        sourceType: SourceType,
        customize: DeltaCoverageConfiguration.() -> Unit
    ): SourcesResolver.Context {
        return SourcesResolver.Context.Builder
            .newBuilder(
                project,
                project.objects,
                project.objects.newInstance(DeltaCoverageConfiguration::class.java).apply(customize)
            )
            .build(sourceType)
    }

    companion object {
        @JvmStatic
        fun `delta coverage source test parameters`(): List<Arguments> {
            return listOf<Arguments>(
                arguments(SourceType.SOURCES, DeltaCoverageConfiguration::srcDirs),
                arguments(SourceType.CLASSES, DeltaCoverageConfiguration::classesDirs),
                arguments(SourceType.COVERAGE_BINARIES, DeltaCoverageConfiguration::coverageBinaryFiles),
            )
        }
    }
}
