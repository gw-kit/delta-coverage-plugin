package io.github.surpsg.deltacoverage.gradle.sources.lookup

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.FileSystem
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.writeText

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KoverPluginSourcesLookupTest {

    private val fileSystem: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    @AfterAll
    fun afterAll() {
        fileSystem.close()
    }

    @Test
    fun `should empty source if kover artifact file not found`() {
        // GIVEN
        val project: Project = testJavaProject {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("org.jetbrains.kotlinx.kover")
            }
        }

        val koverPluginSourcesLookup = KoverPluginSourcesLookup(
            fileSystem,
            SourcesAutoLookup.Context(
                project,
                DeltaCoverageConfiguration(project.objects),
                project.objects
            )
        )

        // WHEN
        val actualSources: FileCollection = koverPluginSourcesLookup.lookup(SourceType.SOURCES)

        // THEN
        actualSources shouldHaveSize 0
    }

    @ParameterizedTest
    @EnumSource(SourceType::class)
    fun `should return source if source found in kover configuration`(
        sourceType: SourceType
    ) {
        // GIVEN
        val project: Project = testJavaProject {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("org.jetbrains.kotlinx.kover")
            }
        }

        createTestResources(project)

        val koverPluginSourcesLookup = KoverPluginSourcesLookup(
            fileSystem,
            SourcesAutoLookup.Context(
                project,
                DeltaCoverageConfiguration(project.objects),
                project.objects
            )
        )

        // WHEN
        val actualSources: FileCollection = koverPluginSourcesLookup.lookup(sourceType)

        // THEN
        actualSources shouldHaveAtLeastSize 1
    }

    private fun createTestResources(project: Project) {
        fileSystem.getPath(project.layout.projectDirectory.asFile.absolutePath)
            .resolve(SOURCES_PATH)
            .createDirectories()

        fileSystem.getPath(project.layout.buildDirectory.asFile.get().absolutePath).apply {
            resolve(CLASSES_PATH).createDirectories()
            resolve(BINARY_COVERAGE_PATH)
                .apply { parent.createDirectories() }
                .createFile()

            resolve("kover")
                .apply { createDirectories() }
                .resolve(KoverPluginSourcesLookup.KOVER_ARTIFACTS_FILE_NAME)
                .writeText(
                    """
                    $SOURCES_PATH
                    
                    build/$CLASSES_PATH
                    
                    build/$BINARY_COVERAGE_PATH
                """.trimIndent()
                )
        }
    }

    companion object {
        const val SOURCES_PATH = "src/main/java"
        const val CLASSES_PATH = "classes/java/main"
        const val BINARY_COVERAGE_PATH = "kover/bin-reports/test.ic"
    }
}
