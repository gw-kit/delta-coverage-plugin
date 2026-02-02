plugins {
    `delta-coverage-conventions`
    `test-report-aggregation`
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
    testMapping {
        enabled = true
        sampling {
            intervalMs = 1
            maxDepth = 50
        }
    }
}
