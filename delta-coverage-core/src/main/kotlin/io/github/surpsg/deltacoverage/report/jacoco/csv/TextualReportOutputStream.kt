package io.github.surpsg.deltacoverage.report.jacoco.csv

import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.textual.TextualReportFacade
import java.io.ByteArrayOutputStream
import java.io.OutputStream

internal class TextualReportOutputStream(
    private val reportType: ReportType,
    private val outputStream: OutputStream,
) : OutputStream() {

    private val byteArrayOutputStream = ByteArrayOutputStream()

    override fun write(b: Int) = byteArrayOutputStream.write(b)

    override fun write(b: ByteArray, off: Int, len: Int) = super.write(b, off, len)

    override fun close() {
        outputStream.use { outStream ->
            val buildContext = TextualReportFacade.BuildContext(
                coverageDataProvider = CsvSourceRawCoverageDataProvider(byteArrayOutputStream.toByteArray()),
                reportType = reportType,
                outputStream = outStream,
            )
            TextualReportFacade.generateReport(buildContext)
        }
        super.close()
    }
}

