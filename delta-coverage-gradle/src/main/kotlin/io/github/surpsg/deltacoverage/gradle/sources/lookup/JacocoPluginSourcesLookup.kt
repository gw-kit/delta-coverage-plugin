package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.sources.lookup.SourcesAutoLookup.Companion.newAutoDetectedSources
import org.gradle.api.file.FileCollection
import org.gradle.testing.jacoco.tasks.JacocoReportBase
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class JacocoPluginSourcesLookup(
    lookupContext: SourcesAutoLookup.Context
) : CacheableLookupSources(lookupContext) {

    override fun lookupSources(lookupContext: SourcesAutoLookup.Context): SourcesAutoLookup.AutoDetectedSources {
        return lookupContext.project.allprojects.asSequence()
            .map { it.tasks.findByName(JACOCO_REPORT_TASK) }
            .filterNotNull()
            .map { it as JacocoReportBase }
            .fold(lookupContext.objectFactory.newAutoDetectedSources()) { jacocoInputs, jacocoReport ->
                log.debug("Found JaCoCo configuration in gradle project '{}'", jacocoReport.project.name)

                jacocoInputs.apply {
                    allBinaryCoverageFiles.from(jacocoReport.executionData)
                    allClasses.from(jacocoReport.allClassDirs)
                }
            }.apply {
                val sourceCodeSources: FileCollection = SourceCodeLookup().lookupSourceCode(lookupContext.project)
                allSources.from(sourceCodeSources)
            }
    }

    companion object {
        const val JACOCO_REPORT_TASK = "jacocoTestReport"

        val log: Logger = LoggerFactory.getLogger(JacocoPluginSourcesLookup::class.java)
    }
}
