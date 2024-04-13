package io.github.surpsg.deltacoverage.report.light.console.asciitable

import io.github.surpsg.deltacoverage.report.light.LightReportRenderer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AsciiTableRendererTest {

    @Test
    fun `should generate table`() {
        // GIVEN
        val outStream = ByteArrayOutputStream()
        val context = LightReportRenderer.Context {
            title = "Title-123"
            output = outStream
            headers = listOf("Header-1", "Header2")
            rows = listOf(
                listOf("Row1Col1", "Row1Col2"),
                listOf("Row2Col1", "Row2Col2")
            )
            footer = listOf("Footer1", "")
        }

        // WHEN
        AsciiTableRenderer.render(context)

        // THEN
        String(outStream.toByteArray()) shouldBe """
            +----------+----------+
            | Title-123           |
            +----------+----------+
            | Header-1 | Header2  |
            +----------+----------+
            | Row1Col1 | Row1Col2 |
            | Row2Col1 | Row2Col2 |
            +----------+----------+
            | Footer1  |          |
            +----------+----------+
            
            """.trimIndent()
    }

    @Test
    fun `should generate empty data table`() {
        // GIVEN
        val outStream = ByteArrayOutputStream()
        val context = LightReportRenderer.Context {
            title = "Title-123"
            output = outStream
            headers = listOf("Header-1", "Header2")
            rows = listOf()
            footer = listOf("Footer1", "")
        }

        // WHEN
        AsciiTableRenderer.render(context)

        // THEN
        val string = String(outStream.toByteArray())
        string shouldBe """
            +----------+---------+
            | Title-123          |
            +----------+---------+
            | Header-1 | Header2 |
            +----------+---------+
            | Footer1  |         |
            +----------+---------+
            
            """.trimIndent()
    }
}
