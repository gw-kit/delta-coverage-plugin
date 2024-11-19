package io.github.surpsg.deltacoverage.gradle.utils

import java.io.File

fun File.resolveByPath(
    maybeRelativePath: String,
): File {
    val file = File(maybeRelativePath)
    return if (file.isAbsolute) {
        file
    } else {
        this.resolve(maybeRelativePath)
    }
}
