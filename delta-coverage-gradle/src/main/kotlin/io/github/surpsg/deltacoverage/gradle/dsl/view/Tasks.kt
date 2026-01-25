package io.github.surpsg.deltacoverage.gradle.dsl.view

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

internal fun TaskProvider<out Task>.dependsOn(anotherTask: TaskProvider<out Task>) = configure {
    it.dependsOn(anotherTask)
}
