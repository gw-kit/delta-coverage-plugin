package io.github.surpsg.deltacoverage.gradle.unittest

import io.mockk.mockk
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder

fun testJavaProject(
    attachSettings: Boolean = false,
    customize: ProjectInternal.() -> Unit = {}
): ProjectInternal =
    (ProjectBuilder.builder().build() as ProjectInternal).also {
        it.plugins.apply("java")

        if (attachSettings) {
            it.gradle.attachSettings(
                mockk(relaxed = true)
            )
        }

        it.customize()
    }
