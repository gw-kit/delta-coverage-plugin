plugins {
    `delta-coverage-conventions`
    `test-report-aggregation`
    alias(deps.plugins.mavenPublish) apply false
}

dependencies {
    allprojects {
        testReportAggregation(this)
    }
}

deltaCoverageReport {
    excludeClasses.addAll(
        "**/deltacoverage/demo/*"
    )
}
