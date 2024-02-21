package io.github.surpsg.deltacoverage.report.jacoco.csv

import com.opencsv.bean.CsvToBeanBuilder
import java.io.InputStreamReader

internal object JacocoCsvReportParser {

    fun parseCsvReport(reader: InputStreamReader): List<CsvCoverageView> {
        return CsvToBeanBuilder<CsvCoverageView>(reader)
            .withType(CsvCoverageView::class.java)
            .build()
            .parse()
    }
}
