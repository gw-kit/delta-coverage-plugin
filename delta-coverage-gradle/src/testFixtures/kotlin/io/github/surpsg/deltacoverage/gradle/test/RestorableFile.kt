package io.github.surpsg.deltacoverage.gradle.test

import java.io.File
import java.io.IOException

class RestorableFile(private val originFileCopy: File, val file: File) {

    @Throws(IOException::class)
    fun restoreOriginContent() {
        originFileCopy.copyTo(file, overwrite = true)
    }
}
