package io.github.surpsg.deltacoverage.gradle.sources.lookup.sourceset

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

internal class SourceSetsLookup {

    fun lookupSourceSets(project: Project): AllSourceSets = with(project) {
        rootProject.allprojects.asSequence()
            .map { proj -> obtainSourcesSets(proj) }
            .fold(AllSourceSets.newSourceSets(project.objects)) { acc, next ->
                acc + next
            }
    }

    private fun obtainSourcesSets(project: Project): AllSourceSets {
        val newSourceSets = AllSourceSets.newSourceSets(project.objects)
        val sourceSetContainer: SourceSetContainer? = project.extensions.findByType(SourceSetContainer::class.java)
        return if (sourceSetContainer != null) {
            val mainSourceSet: SourceSet = sourceSetContainer.getByName(MAIN_SOURCE_SET)
            newSourceSets.apply {
                allSources.from(mainSourceSet.allJava.srcDirs)
                allClasses.from(mainSourceSet.output)
            }
        } else {
            newSourceSets
        }
    }

    internal companion object {
        const val MAIN_SOURCE_SET = "main"
    }
}
