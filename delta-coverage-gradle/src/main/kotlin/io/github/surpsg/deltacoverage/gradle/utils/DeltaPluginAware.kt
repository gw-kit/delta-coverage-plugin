package io.github.surpsg.deltacoverage.gradle.utils

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import org.gradle.api.Project

internal val Project.deltaCoverageConfig: DeltaCoverageConfiguration
    get() = extensions.getByType(
        DeltaCoverageConfiguration::class.java
    )
