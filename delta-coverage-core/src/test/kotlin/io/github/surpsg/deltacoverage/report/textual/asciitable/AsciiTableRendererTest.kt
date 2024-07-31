package io.github.surpsg.deltacoverage.report.textual.asciitable

import io.github.surpsg.deltacoverage.report.textual.TextualReportRenderer
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
        val context = TextualReportRenderer.Context {
            title = "Title-123"
            output = outStream
            headers = listOf("Header-1", "Header2")
            rows = listOf(
                listOf("Row1Col1", "Row1Col2"),
                listOf("Row2Col1", "Row2Col2")
            )
            footer = listOf(
                listOf("Footer1", "1"),
                listOf("Footer2", "2"),
                listOf("Footer3", "3"),
            )
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
            | Footer1  | 1        |
            +----------+----------+
            | Footer2  | 2        |
            +----------+----------+
            | Footer3  | 3        |
            +----------+----------+
            
            """.trimIndent()
    }

    @Test
    fun `should generate empty data table`() {
        // GIVEN
        val outStream = ByteArrayOutputStream()
        val context = TextualReportRenderer.Context {
            title = "Title-123"
            output = outStream
            headers = listOf("Header-1", "Header2")
            rows = listOf()
            footer = listOf(
                listOf("Footer1", ""),
                listOf("", "empty"),
            )
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
            |          | empty   |
            +----------+---------+
            
            """.trimIndent()
    }
}
