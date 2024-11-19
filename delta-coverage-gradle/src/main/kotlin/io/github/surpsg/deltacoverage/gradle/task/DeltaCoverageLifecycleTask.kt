package io.github.surpsg.deltacoverage.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class DeltaCoverageLifecycleTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {

    init {
        outputs.upToDateWhen { false }
    }

    @Input
    val reportDir = objectFactory.property(String::class.java)

    @get:InputFiles
    val summaries: ConfigurableFileCollection = objectFactory.fileCollection()

    @OutputFile
    val aggregatedSummary: RegularFileProperty = objectFactory.fileProperty().convention {
        File(reportDir.get()).resolve("summary.json")
    }

    @TaskAction
    fun executeAction() {
        val aggregatedSummaries: String = summaries.asSequence()
            .map { it.readText() }
            .joinToString(separator = ",", prefix = "[", postfix = "]")

        aggregatedSummary.get().asFile.writeText(aggregatedSummaries)
    }
}
