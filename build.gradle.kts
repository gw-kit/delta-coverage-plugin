plugins {
    java // TODO remove it
    `delta-coverage-conventions`
    `test-report-aggregation`
}

dependencies {
    allprojects {
        testReportAggregation(this)
    }
}
