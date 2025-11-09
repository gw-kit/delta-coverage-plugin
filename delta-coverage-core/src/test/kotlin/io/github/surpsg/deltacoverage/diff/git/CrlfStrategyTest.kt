package io.github.surpsg.deltacoverage.diff.git

import io.kotest.matchers.shouldBe
import org.eclipse.jgit.lib.CoreConfig
import org.junit.jupiter.api.Test

class CrlfStrategyTest {

    @Test
    fun `crlf should be Auto when line separator is CRLF`() {
        val crlf = getCrlf("\r\n")
        crlf shouldBe CoreConfig.AutoCRLF.TRUE
    }

    @Test
    fun `crlf should be Input when line separator is LF`() {
        val crlf = getCrlf("\n")
        crlf shouldBe CoreConfig.AutoCRLF.INPUT
    }
}
