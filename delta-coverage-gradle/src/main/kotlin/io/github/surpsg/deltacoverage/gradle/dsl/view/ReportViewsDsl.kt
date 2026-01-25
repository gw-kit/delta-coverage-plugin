package io.github.surpsg.deltacoverage.gradle.dsl.view

import io.github.surpsg.deltacoverage.gradle.ReportView
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

/**
 * Configures a [ReportView] for the report.
 * If the view is not found, it will be created.
 *
 * @param name The name of the view.
 * @param action The configuration action.
 */
fun NamedDomainObjectContainer<ReportView>.view(name: String, action: Action<in ReportView> = Action {}) {
    val view = maybeCreate(name)
    action.execute(view)
}
