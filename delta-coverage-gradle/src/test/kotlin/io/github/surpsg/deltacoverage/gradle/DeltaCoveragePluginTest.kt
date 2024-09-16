package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.applyPlugin
import io.github.surpsg.deltacoverage.gradle.unittest.newProject
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.matchers.collections.shouldContainExactly
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.junit.jupiter.api.Test

class DeltaCoveragePluginTest {

    @Test
    fun `apply plugin should automatically create views from test tasks`() {
        // GIVEN
        val parentProj: ProjectInternal = testJavaProject(attachSettings = true) {
            newProject {
                withName("child")
                withParent(this@testJavaProject)
            }
            allprojects { proj -> proj.applyPlugin<JavaPlugin>() }
            applyDeltaCoveragePlugin()
        }

        // WHEN
        parentProj.evaluate()

        // THEN
        val config = parentProj.extensions.getByType(DeltaCoverageConfiguration::class.java)
        config.reportViews.names.shouldContainExactly("test")
    }
}
