package io.github.surpsg.deltacoverage.gradle.unittest

import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin
import io.mockk.mockk
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.execution.plan.FinalizedExecutionPlan
import org.gradle.testfixtures.ProjectBuilder

fun testJavaProject(
    attachSettings: Boolean = false,
    customize: ProjectInternal.() -> Unit = {}
): ProjectInternal = newProject().also {
    it.applyPlugin<JavaPlugin>()

    it.repositories.mavenCentral()

    if (attachSettings) {
        it.gradle.attachSettings(
            mockk(relaxed = true)
        )
    }

    it.customize()

    it.evaluate()
    it.gradle.taskGraph.populate(FinalizedExecutionPlan.EMPTY)
}

fun newProject(
    customize: ProjectBuilder.() -> Unit = {}
): ProjectInternal =
    ProjectBuilder.builder()
        .apply(customize)
        .build() as ProjectInternal

inline fun <reified T : Plugin<*>> Project.applyPlugin() = project.pluginManager.apply(T::class.java)

fun Project.applyDeltaCoveragePlugin() = applyPlugin<DeltaCoveragePlugin>()

fun Project.applyKotlinPlugin() = project.pluginManager.apply("org.jetbrains.kotlin.jvm")
