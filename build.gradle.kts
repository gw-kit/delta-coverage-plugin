plugins {
    `delta-coverage-conventions`
    `test-report-aggregation`
}

dependencies {
    allprojects {
        testReportAggregation(this)
    }
}
