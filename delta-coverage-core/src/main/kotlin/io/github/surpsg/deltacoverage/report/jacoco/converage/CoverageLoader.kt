package io.github.surpsg.deltacoverage.report.jacoco.converage

import org.jacoco.core.tools.ExecFileLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.lang.invoke.MethodHandles

internal object CoverageLoader {
    val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun loadExecFiles(
        binaryCoverageFiles: Set<File>
    ): ExecFileLoader {
        val execFileLoader = ExecFileLoader()
        binaryCoverageFiles.forEach {
            log.debug("Loading exec data: {}", it)
            try {
                execFileLoader.load(it)
            } catch (e: IOException) {
                throw RuntimeException("Cannot load coverage data from file: $it", e)
            }
        }
        return execFileLoader
    }
}
