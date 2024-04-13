package io.github.surpsg.deltacoverage.report.jacoco.csv

import io.github.surpsg.deltacoverage.report.light.ConsoleReportFacade
import java.io.ByteArrayOutputStream
import java.io.OutputStream

internal class ConsoleCoverageReportOutputStream(
    private val outputStream: OutputStream
) : OutputStream() {

    private val byteArrayOutputStream = ByteArrayOutputStream()

    override fun write(b: Int) = byteArrayOutputStream.write(b)

    override fun write(b: ByteArray, off: Int, len: Int) = super.write(b, off, len)

    override fun close() {
        outputStream.use { outStream ->
            ConsoleReportFacade.generateReport(
                CsvSourceRawCoverageDataProvider(byteArrayOutputStream.toByteArray()),
                outStream
            )
        }
        super.close()
    }
}

