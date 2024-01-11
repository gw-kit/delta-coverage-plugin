package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.sources.lookup.SourcesAutoLookup.Companion.newAutoDetectedSources
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readLines

internal class KoverPluginSourcesLookup(
    val fileSystem: FileSystem,
    lookupContext: SourcesAutoLookup.Context
) : CacheableLookupSources(lookupContext) {

    override fun lookupSources(lookupContext: SourcesAutoLookup.Context): SourcesAutoLookup.AutoDetectedSources {
        return lookupContext.project.allprojects.asSequence()
            .map { it.tasks.findByName(KOVER_GENERATE_ARTIFACTS_TASK_NAME) }
            .filterNotNull()
            .fold(lookupContext.objectFactory.newAutoDetectedSources()) { koverInputs, koverGenerateArtifactsTask ->
                log.debug("Found Kover configuration in gradle project '{}'", koverGenerateArtifactsTask.project.name)

                koverInputs.apply {
                    applyKoverOutputs(koverGenerateArtifactsTask)
                }
            }
    }

    private fun SourcesAutoLookup.AutoDetectedSources.applyKoverOutputs(
        koverGenerateArtifactsTask: Task
    ) {
        val rootProjectPath: Path = koverGenerateArtifactsTask.resolveRootProjectDirPath()
        koverGenerateArtifactsTask.outputs
            .files
            .asSequence()
            .map { fileSystem.getPath(it.absolutePath) }
            .filter { it.name.endsWith(KOVER_ARTIFACTS_FILE_NAME) }
            .take(1)
            .mapNotNull { it.parseArtifactFile(rootProjectPath) }
            .forEach {
                allBinaryCoverageFiles.from(it.coverageBinaries)
                allClasses.from(it.classFiles)
                allSources.from(it.sources)
            }
    }

    private fun Task.resolveRootProjectDirPath(): Path =
        fileSystem.getPath(project.rootProject.layout.projectDirectory.asFile.absolutePath)

    private fun Path.parseArtifactFile(rootProjectPath: Path): KoverArtifacts? = if (exists()) {
        val iterator: Iterator<String> = readLines().iterator()

        val sources: Set<String> = iterator.readArtifactsSection(rootProjectPath)
        val outputs: Set<String> = iterator.readArtifactsSection(rootProjectPath)
        val coverageBinaries: Set<String> = iterator.readArtifactsSection(rootProjectPath)

        KoverArtifacts(
            coverageBinaries = coverageBinaries,
            classFiles = outputs,
            sources = sources,
        )
    } else {
        null
    }

    private fun Iterator<String>.readArtifactsSection(rootProjectPath: Path): Set<String> {
        return readUntil(String::isBlank)
            .map { rootProjectPath.resolve(it) }
            .filter { it.exists() }
            .map { it.absolutePathString() }
            .toSet()
    }

    private data class KoverArtifacts(
        val coverageBinaries: Set<String>,
        val classFiles: Set<String>,
        val sources: Set<String>,
    )

    private fun Iterator<String>.readUntil(
        interruptCondition: (String) -> Boolean
    ): Sequence<String> = sequence {
        while (hasNext()) {
            val next: String = next()
            if (interruptCondition(next)) {
                break
            }
            yield(next)
        }
    }

    companion object {
        const val KOVER_GENERATE_ARTIFACTS_TASK_NAME = "koverGenerateArtifact"
        const val KOVER_ARTIFACTS_FILE_NAME = "default.artifact"

        val log: Logger = LoggerFactory.getLogger(KoverPluginSourcesLookup::class.java)
    }
}
