package io.github.surpsg.deltacoverage.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReportBase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

open class DeltaCoveragePlugin @Inject constructor(
    private val fileOperations: FileOperations
) : Plugin<Project> {

    override fun apply(project: Project) {

        if (project.isAutoApplyJacocoEnabled()) {
            autoApplyJacocoPlugin(project)
        }

        project.tasks.create(DELTA_COVERAGE_TASK, DeltaCoverageTask::class.java) { deltaCoverageTask ->
            deltaCoverageTask.projectDirProperty.set(project.projectDir)
            deltaCoverageTask.rootProjectDirProperty.set(project.rootProject.projectDir)

            deltaCoverageTask.deltaCoverageReport.set(
                project.extensions.create(
                    DELTA_COVERAGE_REPORT_EXTENSION,
                    DeltaCoverageConfiguration::class.java
                )
            )

            deltaCoverageTask.applyInputsFromJacocoPlugin()

            configureDependencies(project, deltaCoverageTask)
        }
    }

    private fun configureDependencies(project: Project, deltaCoverageTask: DeltaCoverageTask) = project.afterEvaluate {
        project.getAllTasks(true).values.asSequence()
            .flatMap { it.asSequence() }
            .forEach { task ->
                configureDependencyFromTask(deltaCoverageTask, task)
            }
    }

    private fun configureDependencyFromTask(deltaCoverageTask: DeltaCoverageTask, task: Task) {
        if (task.name == JavaPlugin.CLASSES_TASK_NAME) {
            log.info("Configuring {} to depend on {}", deltaCoverageTask, task)
            deltaCoverageTask.dependsOn(task)
        }

        if (task is Test) {
            val isJacocoExtensionConfigured = task.extensions.findByType(JacocoTaskExtension::class.java) != null
            if (isJacocoExtensionConfigured) {
                log.info("Configuring {} to run after {}", deltaCoverageTask, task)
                deltaCoverageTask.mustRunAfter(task)
            }
        }
    }

    private fun DeltaCoverageTask.applyInputsFromJacocoPlugin() = project.gradle.taskGraph.whenReady {
        val jacocoPluginInputs: JacocoInputs = collectJacocoPluginInputs(project)
        jacocoExecFiles.set(jacocoPluginInputs.allExecFiles)
        jacocoClassesFiles.set(jacocoPluginInputs.allClasses)
        jacocoSourceFiles.set(jacocoPluginInputs.allSources)
    }

    private fun autoApplyJacocoPlugin(project: Project) {
        val jacocoApplied: Boolean = project.allprojects.any {
            it.pluginManager.hasPlugin(JACOCO_PLUGIN)
        }
        if (!jacocoApplied) {
            project.allprojects.forEach {
                log.info("Auto-applying $JACOCO_PLUGIN plugin to project '{}'", it.name)
                it.pluginManager.apply(JACOCO_PLUGIN)
            }
        }
    }

    private fun Project.isAutoApplyJacocoEnabled(): Boolean {
        val autoApplyValue = project.properties.getOrDefault(AUTO_APPLY_JACOCO_PROPERTY_NAME, "true")!!
        return autoApplyValue.toString().toBoolean()
    }

    private fun collectJacocoPluginInputs(project: Project): JacocoInputs {
        return project.allprojects.asSequence()
            .map { it.tasks.findByName(JACOCO_REPORT_TASK) }
            .filterNotNull()
            .map { it as JacocoReportBase }
            .fold(newJacocoInputs()) { jacocoInputs, jacocoReport ->
                log.debug("Found JaCoCo configuration in gradle project '{}'", jacocoReport.project.name)

                jacocoInputs.apply {
                    allExecFiles.from(jacocoReport.executionData)
                    allClasses.from(jacocoReport.allClassDirs)
                    allSources.from(jacocoReport.allSourceDirs)
                }
            }
    }

    private fun newJacocoInputs() = JacocoInputs(
        // FileOperations is used to support Gradle < v5.3
        // If min supported Gradle version is 5.3 then it could be replaced with ObjectFactory#fileCollection
        fileOperations.configurableFiles(),
        fileOperations.configurableFiles(),
        fileOperations.configurableFiles()
    )

    private class JacocoInputs(
        val allExecFiles: ConfigurableFileCollection,
        val allClasses: ConfigurableFileCollection,
        val allSources: ConfigurableFileCollection
    )

    companion object {
        const val AUTO_APPLY_JACOCO_PROPERTY_NAME = "io.github.surpsg.delta-coverage.auto-apply-jacoco"
        const val DELTA_COVERAGE_REPORT_EXTENSION = "deltaCoverageReport"
        const val DELTA_COVERAGE_TASK = "deltaCoverage"
        const val JACOCO_PLUGIN = "jacoco"
        const val JACOCO_REPORT_TASK = "jacocoTestReport"

        val log: Logger = LoggerFactory.getLogger(DeltaCoveragePlugin::class.java)
    }

}
