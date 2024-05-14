package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.sources.lookup.SourceSetsLookup.AutoDetectedSources.Companion.newAutoDetectedSourceSets
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

internal class SourceSetsLookup {

    fun lookupSourceSets(project: Project): AutoDetectedSources = with(project) {
        rootProject.allprojects.asSequence()
            .map { proj -> obtainSourcesSets(proj) }
            .fold(project.objects.newAutoDetectedSourceSets()) { acc, next ->
                acc + next
            }
    }

    private fun obtainSourcesSets(project: Project): AutoDetectedSources {
        val newSourceSets = project.objects.newAutoDetectedSourceSets()
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

    internal data class AutoDetectedSources(
        val allClasses: ConfigurableFileCollection,
        val allSources: ConfigurableFileCollection,
    ) {

        operator fun plus(sourceSets: AutoDetectedSources): AutoDetectedSources = apply {
            allSources.from(sourceSets.allSources)
            allClasses.from(sourceSets.allClasses)
        }

        companion object {

            fun ObjectFactory.newAutoDetectedSourceSets() = AutoDetectedSources(
                fileCollection(),
                fileCollection(),
            )
        }
    }

    internal companion object {
        const val MAIN_SOURCE_SET = "main"
    }
}
