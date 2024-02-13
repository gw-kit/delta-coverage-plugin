import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

plugins {
    kotlin("jvm")
    id("basic-coverage-conventions")
}

apply<io.gradle.surpsg.deltacoverage.testkit.IntellijCoverageGradleTestKitPlugin>()

tasks.named("koverGenerateArtifact") {
    doLast {
        val outputFile: File = outputs.files.single { it.endsWith("default.artifact") }
        val iterator: Iterator<String> = outputFile
            .readLines()
            .iterator()

        val rootProjectPath: Path = project.rootProject.layout.projectDirectory.asFile.toPath()

        val sourceDirectories: FileCollection = sourceSets.main.get().allJava.sourceDirectories
        val sources: Set<String> = iterator.readArtifactsSection(rootProjectPath)
            .filter { sourceDirectories.contains(file(it)) }
            .toSet()

        val classesDirectories: FileCollection = sourceSets.main.get().output.classesDirs
        val outputs: Set<String> = iterator.readArtifactsSection(rootProjectPath)
            .filter { classesDirectories.contains(file(it)) }
            .toSet()

        val coverageBinaries: Set<String> = iterator.readArtifactsSection(rootProjectPath).toSet() +
                project.layout.buildDirectory.asFileTree.matching { include("coverage/*.ic") }
                    .files.asSequence()
                    .map { it.absolutePath }
                    .toSet()

        val filteredFileResources: String = sequenceOf(
            sources,
            setOf(""),
            outputs,
            setOf(""),
            coverageBinaries,
        ).flatten().joinToString(separator = "\n")
        outputFile.writeText(filteredFileResources)
    }
}

private fun Iterator<String>.readArtifactsSection(rootProjectPath: Path): Sequence<String> {
    return readUntil(String::isBlank)
        .map { rootProjectPath.resolve(it) }
        .filter { it.exists() }
        .map { it.absolutePathString() }
        .distinct()
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
