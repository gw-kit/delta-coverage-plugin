package io.github.surpsg.deltacoverage.gradle

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class DeltaCoverageSourcesAutoConfigurator(
    private val deltaCoverageReportProperty: Property<DeltaCoverageConfiguration>,
    private val jacocoExecFiles: FileCollection,
    private val jacocoClasses: FileCollection,
    private val jacocoSources: FileCollection
) {

    private val deltaCoverageConfiguration: DeltaCoverageConfiguration
        get() = deltaCoverageReportProperty.get()

    fun obtainExecFiles(): FileCollection {
        return collectFileCollectionOrThrow(ConfigurationSourceType.EXEC)
    }

    fun obtainSourcesFiles(): FileCollection {
        return collectFileCollectionOrThrow(ConfigurationSourceType.SOURCES)
    }

    fun obtainClassesFiles(): FileCollection {
        return collectFileCollectionOrThrow(ConfigurationSourceType.CLASSES)
    }

    private fun collectFileCollectionOrThrow(sourceType: ConfigurationSourceType): FileCollection {
        val (collectionSource, fileCollection) = collectFileCollectionOrAutoconfigure(sourceType)
        return if (fileCollection.isEmpty) {
            throwMissedConfigurationException(collectionSource, sourceType)
        } else {
            log.debug(
                "{}({}) was configured from {}",
                sourceType.sourceConfigurationPath,
                sourceType.resourceName,
                collectionSource.pluginName
            )
            fileCollection
        }
    }

    private fun throwMissedConfigurationException(
        collectionSource: FileCollectionSource,
        sourceType: ConfigurationSourceType
    ): Nothing {
        val errorMessage = if (collectionSource == FileCollectionSource.DELTA_COVERAGE) {
            "'${sourceType.sourceConfigurationPath}' file collection is empty."
        } else {
            "'${sourceType.sourceConfigurationPath}' is not configured."
        }
        throw IllegalArgumentException(errorMessage)
    }

    private fun collectFileCollectionOrAutoconfigure(
        configurationType: ConfigurationSourceType
    ): Pair<FileCollectionSource, FileCollection> {
        val configurationSource: ConfigurationSource = obtainConfigurationSource(configurationType)
        return if (configurationSource.customConfigurationFiles != null) {
            FileCollectionSource.DELTA_COVERAGE to configurationSource.customConfigurationFiles
        } else {
            log.debug(
                "{} is not configured. Attempting to autoconfigure from JaCoCo...",
                configurationType.sourceConfigurationPath
            )
            FileCollectionSource.JACOCO to configurationSource.jacocoConfigurationFiles
        }
    }

    private fun obtainConfigurationSource(
        configurationSourceType: ConfigurationSourceType
    ): ConfigurationSource = when (configurationSourceType) {
        ConfigurationSourceType.CLASSES -> ConfigurationSource(
            jacocoClasses,
            deltaCoverageConfiguration.classesDirs
        )

        ConfigurationSourceType.SOURCES -> ConfigurationSource(
            jacocoSources,
            deltaCoverageConfiguration.srcDirs
        )

        ConfigurationSourceType.EXEC -> ConfigurationSource(
            jacocoExecFiles,
            deltaCoverageConfiguration.jacocoExecFiles
        )
    }

    private class ConfigurationSource(
        val jacocoConfigurationFiles: FileCollection,
        val customConfigurationFiles: FileCollection?
    )

    internal enum class ConfigurationSourceType(
        val sourceConfigurationPath: String,
        val resourceName: String
    ) {
        CLASSES("deltaCoverageReport.classesDirs", ".class files"),
        SOURCES("deltaCoverageReport.srcDirs", "sources"),
        EXEC("deltaCoverageReport.jacocoExecFiles", ".exec files")
    }

    private enum class FileCollectionSource(val pluginName: String) {
        JACOCO("JaCoCo"), DELTA_COVERAGE("Diff-Coverage")
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(DeltaCoverageSourcesAutoConfigurator::class.java)
    }

}
