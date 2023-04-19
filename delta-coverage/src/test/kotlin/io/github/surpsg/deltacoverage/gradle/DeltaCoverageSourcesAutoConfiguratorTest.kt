package io.github.surpsg.deltacoverage.gradle

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.testfixtures.ProjectBuilder

class DeltaCoverageSourcesAutoConfiguratorTest : StringSpec() {

    private val project: Project = ProjectBuilder.builder().build()
    private val emptyFileCollection: ConfigurableFileCollection = project.files()

    init {

        "get input file collection should throw when source files are not specified" {
            forAll(
                row(
                    "'deltaCoverageReport.jacocoExecFiles' is not configured.",
                    DeltaCoverageSourcesAutoConfigurator::obtainExecFiles
                ),
                row(
                    "'deltaCoverageReport.classesDirs' is not configured.",
                    DeltaCoverageSourcesAutoConfigurator::obtainClassesFiles
                ),
                row(
                    "'deltaCoverageReport.srcDirs' is not configured.",
                    DeltaCoverageSourcesAutoConfigurator::obtainSourcesFiles
                )
            ) { expectedError, sourceAccessor ->
                // setup
                val autoConfigurator = DeltaCoverageSourcesAutoConfigurator(
                    property(DeltaCoverageConfiguration()),
                    emptyFileCollection,
                    emptyFileCollection,
                    emptyFileCollection
                )

                // run
                val exception = shouldThrow<IllegalArgumentException> {
                    sourceAccessor(autoConfigurator)
                }

                // assert
                exception.message shouldBeEqualIgnoringCase expectedError
            }

        }

        "get input file collection should throw when file collection is empty" {
            forAll(
                row(
                    "'deltaCoverageReport.jacocoExecFiles' file collection is empty.",
                    DeltaCoverageSourcesAutoConfigurator::obtainExecFiles
                ),
                row(
                    "'deltaCoverageReport.classesDirs' file collection is empty.",
                    DeltaCoverageSourcesAutoConfigurator::obtainClassesFiles
                ),
                row(
                    "'deltaCoverageReport.srcDirs' file collection is empty.",
                    DeltaCoverageSourcesAutoConfigurator::obtainSourcesFiles
                )
            ) { expectedError, sourceAccessor ->
                // setup
                val deltaCoverageReport = DeltaCoverageConfiguration().apply {
                    jacocoExecFiles = emptyFileCollection
                    classesDirs = emptyFileCollection
                    srcDirs = emptyFileCollection
                }
                val autoConfigurator = DeltaCoverageSourcesAutoConfigurator(
                    property(deltaCoverageReport),
                    emptyFileCollection,
                    emptyFileCollection,
                    emptyFileCollection
                )

                // run
                val exception = shouldThrow<IllegalArgumentException> {
                    sourceAccessor(autoConfigurator)
                }

                // assert
                exception.message shouldBeEqualIgnoringCase expectedError
            }
        }

    }

    private inline fun <reified T> property(propertyValue: T): Property<T> {
        return project.objects.property(T::class.java).apply {
            set(propertyValue)
        }
    }

}
