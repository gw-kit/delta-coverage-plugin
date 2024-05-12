package io.github.surpsg.deltacoverage.gradle.sources.lookup

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

internal class SourceCodeLookup {

    fun lookupSourceCode(project: Project): FileCollection = with(project) {
        files(
            provider { findAllSources() }
        )
    }

    private fun Project.findAllSources(): FileCollection {
        return project.rootProject.allprojects.asSequence()
            .map { proj -> sources(proj) }
            .fold(project.files() as FileCollection) { acc, next ->
                acc + next
            }
    }

    private fun sources(project: Project): FileCollection {
        val sourceSetContainer: SourceSetContainer? = project.extensions.findByType(SourceSetContainer::class.java)
        val dirsSet: Set<File> = if (sourceSetContainer != null) {
            sourceSetContainer.getByName(MAIN_SOURCE_SET).allJava.srcDirs
        } else {
            emptySet()
        }
        return project.files(dirsSet)
    }

    internal companion object {
        const val MAIN_SOURCE_SET = "main"
    }
}
