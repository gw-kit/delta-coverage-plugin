package io.github.surpsg.deltacoverage.gradle.utils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

class FilesTest {

    @Test
    fun `should return file as is if path is absolute`() {
        val actual = File(".").resolveByPath("/absolute/path")

        actual shouldBe File("/absolute/path")
    }

    @Test
    fun `should return file resolved to base path`() {
        val actual = File("/tmp/").resolveByPath("../relative/path")

        actual shouldBe File("/tmp/../relative/path")
    }
}
