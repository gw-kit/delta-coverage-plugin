package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.utils.lazyFileCollection
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readLines

internal class KoverPluginSourcesLookup(
    private val fileSystem: FileSystem,
    lookupContext: SourcesAutoLookup.Context
) : CacheableLookupSources(lookupContext) {

    override fun lookupCoverageBinaries(lookupContext: SourcesAutoLookup.Context): FileCollection {
        return lookupContext.project.lazyFileCollection {
            lookupContext.project.allprojects.asSequence()
                .mapNotNull { it.tasks.findByName(KOVER_GENERATE_ARTIFACTS_TASK_NAME) }
                .fold(setOf<String>()) { allBinaries, koverGenerateArtifactsTask ->
                    log.debug(
                        "Found Kover configuration in gradle project '{}'",
                        koverGenerateArtifactsTask.project.name
                    )

                    allBinaries + obtainKoverBinaries(lookupContext.viewName, koverGenerateArtifactsTask)
                }
                .let { lookupContext.project.files(it) }
        }
    }

    private fun obtainKoverBinaries(
        viewName: String,
        koverGenerateArtifactsTask: Task,
    ): Set<String> {
        val rootProjectPath: Path = koverGenerateArtifactsTask.resolveRootProjectDirPath()
        return koverGenerateArtifactsTask.outputs
            .files
            .asSequence()
            .map { fileSystem.getPath(it.absolutePath) }
            .filter { it.name.endsWith(KOVER_ARTIFACTS_FILE_NAME) }
            .take(1)
            .mapNotNull { it.parseArtifactFile(viewName, rootProjectPath) }
            .flatMap { it }
            .toSet()
    }

    private fun Task.resolveRootProjectDirPath(): Path =
        fileSystem.getPath(project.rootProject.layout.projectDirectory.asFile.absolutePath)

    private fun Path.parseArtifactFile(
        viewName: String,
        rootProjectPath: Path,
    ): Set<String>? = if (exists()) {
        val iterator: Iterator<String> = readLines().iterator()

        iterator.readArtifactsSection(rootProjectPath)
        iterator.readArtifactsSection(rootProjectPath)

        val coverageBinaries: Set<String> = iterator.readArtifactsSection(rootProjectPath)
            .asSequence()
            .filter { path -> path.endsWith("/$viewName.ic") }
            .toSet()
        coverageBinaries
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
