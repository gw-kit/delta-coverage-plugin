package io.github.surpsg.deltacoverage.gradle.utils

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

fun Project.lazyFileCollection(supplier: () -> FileCollection): FileCollection = files(
    provider {
        supplier()
    }
)
