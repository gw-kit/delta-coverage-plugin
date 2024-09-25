package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.utils.lazyFileCollection
import org.gradle.api.file.FileCollection

import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

internal class JacocoPluginSourcesLookup(
    lookupContext: SourcesAutoLookup.Context
) : CacheableLookupSources(lookupContext) {

    override fun lookupCoverageBinaries(lookupContext: SourcesAutoLookup.Context): FileCollection {
        return lookupContext.project.lazyFileCollection {
            collectBinaryFiles(lookupContext)
        }
    }

    private fun collectBinaryFiles(lookupContext: SourcesAutoLookup.Context): FileCollection {
        return lookupContext.project.allprojects.asSequence()
            .mapNotNull { project -> project.tasks.findByName(lookupContext.viewName) }
            .mapNotNull { task -> task.extensions.findByType(JacocoTaskExtension::class.java) }
            .mapNotNull { jacocoExtension -> jacocoExtension.destinationFile }
            .onEach {
                log.debug(
                    "[{}] Found coverage binary: project={}, file={}",
                    lookupContext.viewName,
                    lookupContext.project.name,
                    it,
                )
            }
            .fold(lookupContext.project.objects.fileCollection()) { allBinaries, execFile ->
                allBinaries.from(execFile)
            }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    }
}
