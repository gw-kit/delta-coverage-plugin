package io.github.surpsg.deltacoverage.report.textual

internal interface RawCoverageDataProvider {

    fun obtainData(): List<RawCoverageData>
}
