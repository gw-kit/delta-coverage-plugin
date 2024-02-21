package io.github.surpsg.deltacoverage.report.console

internal interface RawCoverageDataProvider {

    fun obtainData(): List<RawCoverageData>
}
