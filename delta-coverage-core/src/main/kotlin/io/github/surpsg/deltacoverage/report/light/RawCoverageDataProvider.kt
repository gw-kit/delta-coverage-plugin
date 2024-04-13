package io.github.surpsg.deltacoverage.report.light

internal interface RawCoverageDataProvider {

    fun obtainData(): List<RawCoverageData>
}
