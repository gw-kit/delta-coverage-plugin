package io.github.surpsg.deltacoverage.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.DefaultExecSpec
import org.gradle.process.internal.ExecAction
import org.gradle.process.internal.ExecActionFactory
import org.gradle.work.DisableCachingByDefault
import java.io.File
import javax.inject.Inject

@DisableCachingByDefault(because = "Should not cache the diff file.")
@Suppress("UnnecessaryAbstractClass")
abstract class NativeGitDiffTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val execActionFactory: ExecActionFactory,
) : DefaultTask() {

    @get:Input
    val targetBranch: Property<String> = objectFactory.property(String::class.java)

    @get:OutputFile
    val diffFile: RegularFileProperty = objectFactory.fileProperty()
        .convention(project.layout.buildDirectory.file("code-diff.diff"))

    private val execSpec: DefaultExecSpec = objectFactory.newInstance(DefaultExecSpec::class.java)

    init {
        description = "Generate a diff file using native git."
    }

    @TaskAction
    fun obtainDiff() {
        val file: File = diffFile.get().asFile

        val execAction: ExecAction = execActionFactory.newExecAction()
        file.outputStream().use { fileOutputStream ->
            execSpec.run {
                commandLine = listOf("git", "diff", "--no-color", "--minimal", targetBranch.get())
                standardOutput = fileOutputStream
                copyTo(execAction)
                execAction.execute()
            }

            println("Diff file generated: file://${file.absolutePath}")
        }
    }
}
