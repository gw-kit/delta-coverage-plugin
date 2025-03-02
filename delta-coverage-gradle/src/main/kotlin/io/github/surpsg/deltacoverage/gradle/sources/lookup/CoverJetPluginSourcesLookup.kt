package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.gwkit.coverjet.gradle.task.CovAgentProperties
import io.github.surpsg.deltacoverage.gradle.utils.lazyFileCollection
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class CoverJetPluginSourcesLookup(
    lookupContext: SourcesAutoLookup.Context
) : CacheableLookupSources(lookupContext) {

    override fun lookupCoverageBinaries(lookupContext: SourcesAutoLookup.Context): FileCollection {
        return lookupContext.project.lazyFileCollection {
            lookupContext.project.allprojects.collectBinaryCoverageFiles(lookupContext)
                .let { lookupContext.project.files(it) }
        }
    }

    private fun Iterable<Project>.collectBinaryCoverageFiles(
        lookupContext: SourcesAutoLookup.Context,
    ): List<Provider<String>> {
        return asSequence()
            .flatMap { proj -> proj.findCoverageBinaryProviders(lookupContext.viewName) }
            .onEach { covAgentPropertiesTask ->
                log.info(
                    "[{}] Found CoverJet configuration in gradle project '{}'",
                    lookupContext.viewName,
                    covAgentPropertiesTask.project,
                )
            }
            .map { it.binaryCoverageFilePath }
            .toList()
    }

    private fun Project.findCoverageBinaryProviders(viewName: String): TaskCollection<CovAgentProperties> {
        return tasks.withType(CovAgentProperties::class.java)
            .matching { it.taskName.get() == viewName }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CoverJetPluginSourcesLookup::class.java)
    }
}
