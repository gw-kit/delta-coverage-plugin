package io.github.surpsg.deltacoverage.report.jacoco.converage

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import java.io.File

class CoverageLoaderTest {

    @Test
    fun `should throw if file does not exist`() {
        // GIVEN
        val files = setOf(
            File("/some/unknown/path/for/sure")
        )

        // WHEN // THEN
        shouldThrow<RuntimeException> {
            CoverageLoader.loadExecFiles(files)
        }
    }
}
