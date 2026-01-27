package io.github.surpsg.deltacoverage.gradle.reportview

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

internal object ViewLookup {

    fun lookup(
        project: Project,
        viewConsumer: (Project, String) -> Unit,
    ) {
        project.allprojects { proj ->
            proj.tasks.withType(Test::class.java) { testTask ->
                val viewName: String = testTask.name
                viewConsumer(testTask.project, viewName)
            }
        }
    }
}
