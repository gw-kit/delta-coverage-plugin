package io.github.surpsg.deltacoverage.diff

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class UrlDiffSourceTest {

    @Test
    fun `pullDiff should throw when url is invalid`() {
        // GIVEN
        val urlDiffSource = UrlDiffSource("invalid url format")

        // WHEN // THEN
        shouldThrow<IllegalArgumentException> {
            urlDiffSource.pullDiff()
        }
    }
}
