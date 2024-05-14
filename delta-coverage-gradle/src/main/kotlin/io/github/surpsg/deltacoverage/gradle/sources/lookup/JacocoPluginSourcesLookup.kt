package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.sources.lookup.SourcesAutoLookup.Companion.newAutoDetectedSources
import io.github.surpsg.deltacoverage.gradle.sources.lookup.sourceset.AllSourceSets
import io.github.surpsg.deltacoverage.gradle.sources.lookup.sourceset.SourceSetsLookup
import org.gradle.api.file.FileCollection
import org.gradle.testing.jacoco.tasks.JacocoReportBase
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class JacocoPluginSourcesLookup(
    lookupContext: SourcesAutoLookup.Context
) : CacheableLookupSources(lookupContext) {

    override fun lookupSources(lookupContext: SourcesAutoLookup.Context): SourcesAutoLookup.AutoDetectedSources {
        val jacocoBinaries: FileCollection = obtainJacocoBinaries(lookupContext)
        val sourceCodeSources: AllSourceSets = SourceSetsLookup().lookupSourceSets(lookupContext.project)
        return lookupContext.objectFactory.newAutoDetectedSources().apply {
            allSources.from(sourceCodeSources.allSources)
            allClasses.from(sourceCodeSources.allClasses)
            allBinaryCoverageFiles.from(jacocoBinaries)
        }
    }

    private fun obtainJacocoBinaries(lookupContext: SourcesAutoLookup.Context): FileCollection {
        return lookupContext.project.allprojects.asSequence()
            .map { it.tasks.findByName(JACOCO_REPORT_TASK) }
            .filterNotNull()
            .map { it as JacocoReportBase }
            .fold(lookupContext.project.objects.fileCollection() as FileCollection) { allBinaries, jacocoReport ->
                log.debug("Found JaCoCo configuration in gradle project '{}'", jacocoReport.project.name)
                allBinaries + jacocoReport.executionData
            }
    }

    companion object {
        const val JACOCO_REPORT_TASK = "jacocoTestReport"

        val log: Logger = LoggerFactory.getLogger(JacocoPluginSourcesLookup::class.java)
    }
}
