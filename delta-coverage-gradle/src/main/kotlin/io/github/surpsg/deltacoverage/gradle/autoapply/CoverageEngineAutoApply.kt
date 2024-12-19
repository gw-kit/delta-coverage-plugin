package io.github.surpsg.deltacoverage.gradle.autoapply

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.Coverage
import io.github.surpsg.deltacoverage.gradle.utils.deltaCoverageConfig
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class CoverageEngineAutoApply {

    fun applyEngine(
        project: Project,
    ) = project.afterEvaluate { thisProject ->
        val coverageConfiguration: Coverage = project.deltaCoverageConfig.coverage
        if (coverageConfiguration.autoApplyPlugin.get()) {
            thisProject.autoApplyCoverageEngine(coverageConfiguration.engine.get())
        }
    }

    private fun Project.autoApplyCoverageEngine(engine: CoverageEngine) {
        val pluginId: String = when (engine) {
            CoverageEngine.JACOCO -> JACOCO_PLUGIN_ID
            CoverageEngine.INTELLIJ -> KOVER_PLUGIN_ID
        }
        allprojects.forEach {
            log.info("Auto-applying {} plugin to project '{}'", pluginId, it.name)
            it.pluginManager.apply(pluginId)
        }
    }

    companion object {
        const val JACOCO_PLUGIN_ID = "jacoco"
        const val KOVER_PLUGIN_ID = "org.jetbrains.kotlinx.kover"

        val log: Logger = LoggerFactory.getLogger(CoverageEngineAutoApply::class.java)
    }
}
