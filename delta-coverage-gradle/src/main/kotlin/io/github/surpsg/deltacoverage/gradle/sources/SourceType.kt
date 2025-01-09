package io.github.surpsg.deltacoverage.gradle.sources

internal enum class SourceType(
    val sourceConfigurationPath: String,
    private val genericResourceName: String
) {
    CLASSES("deltaCoverageReport.classesDirs", ".class files"),
    SOURCES("deltaCoverageReport.sources", "sources"),
    COVERAGE_BINARIES(
        "deltaCoverageReport.reportViews.<view>.binaryCoverageFiles",
        ""
    );

    fun resourceName(sourceProvider: SourcesResolver.Provider): String {
        return when (this) {
            COVERAGE_BINARIES -> binariesResourceName(sourceProvider)
            else -> genericResourceName
        }
    }

    private fun binariesResourceName(sourceProvider: SourcesResolver.Provider): String {
        return when (sourceProvider) {
            SourcesResolver.Provider.JACOCO -> "'.exec' files"
            SourcesResolver.Provider.KOVER -> "'.ic' files"
            SourcesResolver.Provider.DELTA_COVERAGE -> sourceConfigurationPath
        }
    }
}
