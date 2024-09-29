package io.github.surpsg.deltacoverage.gradle.reportview

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import java.util.concurrent.ConcurrentHashMap

internal object ViewLookup {

    fun lookup(
        project: Project,
        viewNameConsumer: (String) -> Unit,
    ) {
        val evaluatedTestTasks: MutableSet<String> = ConcurrentHashMap.newKeySet()
        project.allprojects { proj ->
            proj.tasks.withType(Test::class.java) { testTask ->
                val viewName: String = testTask.name
                val isNewItem = evaluatedTestTasks.add(viewName)
                if (isNewItem) {
                    viewNameConsumer(viewName)
                }
            }
        }
    }
}
