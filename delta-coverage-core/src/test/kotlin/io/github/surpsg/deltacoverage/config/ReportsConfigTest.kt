package io.github.surpsg.deltacoverage.config

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.string.shouldBeEmpty
import org.junit.jupiter.api.Test

class ReportsConfigTest {

    @Test
    fun `should build reports config with defaults`() {
        val actualConfig = ReportsConfig {}

        assertSoftly(actualConfig) {
            html shouldBeEqualToComparingFields ReportConfig {}
            xml shouldBeEqualToComparingFields ReportConfig {}
            markdown shouldBeEqualToComparingFields ReportConfig {}
            console shouldBeEqualToComparingFields ReportConfig {}
            baseReportDir.shouldBeEmpty()
            fullCoverageReport.shouldBeFalse()
        }
    }

    @Test
    fun `should build reports config with custom properties`() {
        // GIVEN
        val expectedBaseReportDir = "test base report"
        val expectedHtmlReportName = "test html"
        val expectedXmlReportName = "test xml"
        val expectedMarkdownReportName = "test markdown"

        // WHEN
        val actualConfig = ReportsConfig {
            html = ReportConfig { enabled = true; outputFileName = expectedHtmlReportName }
            xml = ReportConfig { enabled = true; outputFileName = expectedXmlReportName }
            markdown = ReportConfig { enabled = true; outputFileName = expectedMarkdownReportName }
            console = ReportConfig { enabled = true }
            baseReportDir = expectedBaseReportDir
            fullCoverageReport = true
        }

        // THEN
        assertSoftly(actualConfig) {
            assertSoftly(html) {
                enabled.shouldBeTrue()
                outputFileName shouldBeEqualComparingTo expectedHtmlReportName
            }
            assertSoftly(xml) {
                enabled.shouldBeTrue()
                outputFileName shouldBeEqualComparingTo expectedXmlReportName
            }
            assertSoftly(markdown) {
                enabled.shouldBeTrue()
                outputFileName shouldBeEqualComparingTo expectedMarkdownReportName
            }
            assertSoftly(console) {
                enabled.shouldBeTrue()
            }
            baseReportDir shouldBeEqualComparingTo expectedBaseReportDir
            fullCoverageReport.shouldBeTrue()
        }
    }
}
