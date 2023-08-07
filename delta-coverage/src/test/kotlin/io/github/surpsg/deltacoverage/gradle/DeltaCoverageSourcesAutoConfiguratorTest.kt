package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.sources.lookup.JacocoPluginSourcesLookup
import io.github.surpsg.deltacoverage.gradle.sources.lookup.SourcesAutoLookup
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.testfixtures.ProjectBuilder

class DeltaCoverageSourcesAutoConfiguratorTest : StringSpec() {

    private val project: Project = ProjectBuilder.builder().build()
    private val emptyFileCollection: ConfigurableFileCollection = project.files()

    init {

        "get input file collection should throw when source files are not specified" {
            forAll(
                row(
                    SourceType.COVERAGE_BINARIES,
                    "'deltaCoverageReport.jacocoExecFiles' is not configured."
                ),
                row(
                    SourceType.CLASSES,
                    "'deltaCoverageReport.classesDirs' is not configured.",
                ),
                row(
                    SourceType.SOURCES,
                    "'deltaCoverageReport.srcDirs' is not configured.",
                )
            ) { source, expectedError ->
                // setup
                val autoConfigurator = JacocoPluginSourcesLookup(
                    SourcesAutoLookup.Context(
                        project,
                        DeltaCoverageConfiguration(project.objects),
                        project.objects
                    )
                )

                // run
                val exception = shouldThrow<IllegalArgumentException> {
                    autoConfigurator.lookup(source)
                }

                // assert
                exception.message shouldBeEqualIgnoringCase expectedError
            }

        }

        "get input file collection should throw when file collection is empty" {
            forAll(
                row(
                    SourceType.COVERAGE_BINARIES,
                    "'deltaCoverageReport.jacocoExecFiles' file collection is empty.",
                ),
                row(
                    SourceType.COVERAGE_BINARIES,
                    "'deltaCoverageReport.classesDirs' file collection is empty.",
                ),
                row(
                    SourceType.COVERAGE_BINARIES,
                    "'deltaCoverageReport.srcDirs' file collection is empty.",
                )
            ) { source, expectedError ->
                // setup
                val deltaCoverageConfig = DeltaCoverageConfiguration(project.objects).apply {
                    coverageBinaryFiles = emptyFileCollection
                    classesDirs = emptyFileCollection
                    srcDirs = emptyFileCollection
                }
                val autoConfigurator = JacocoPluginSourcesLookup(
                    SourcesAutoLookup.Context(
                        project,
                        deltaCoverageConfig,
                        project.objects
                    )
                )

                // run
                val exception = shouldThrow<IllegalArgumentException> {
                    autoConfigurator.lookup(source)
                }

                // assert
                exception.message shouldBeEqualIgnoringCase expectedError
            }
        }
    }

}
