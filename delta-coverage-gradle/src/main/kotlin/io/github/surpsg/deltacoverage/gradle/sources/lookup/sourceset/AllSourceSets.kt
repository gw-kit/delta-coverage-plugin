package io.github.surpsg.deltacoverage.gradle.sources.lookup.sourceset

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory

internal data class AllSourceSets(
    val allClasses: ConfigurableFileCollection,
    val allSources: ConfigurableFileCollection,
) {

    operator fun plus(sourceSets: AllSourceSets): AllSourceSets = apply {
        allSources.from(sourceSets.allSources)
        allClasses.from(sourceSets.allClasses)
    }

    companion object {

        fun newSourceSets(objFactory: ObjectFactory) = AllSourceSets(
            objFactory.fileCollection(),
            objFactory.fileCollection(),
        )
    }
}
