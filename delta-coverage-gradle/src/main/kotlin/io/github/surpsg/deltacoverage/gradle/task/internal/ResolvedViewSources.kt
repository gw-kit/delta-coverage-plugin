package io.github.surpsg.deltacoverage.gradle.task.internal

import java.io.File

internal data class ResolvedViewSources(
    val sources: Set<File>,
    val classes: Set<File>,
    val coverageBinaries: Set<File>,
)
