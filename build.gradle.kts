plugins {
    base
    `delta-coverage-conventions`
    `test-report-aggregation`
    `coverage-aggregate-conventions`
    alias(deps.plugins.depUpdatesPlugin)
}

repositories {
    mavenCentral()
}

dependencies {
    allprojects {
        testReportAggregation(this)
    }
}

tasks.named("check") {
    dependsOn(
        provider {
            subprojects.map { it.tasks.named("check") }
        }
    )
}

deltaCoverageReport {
    reportViews.create("aza") {
        coverageBinaryFiles = files("build/reports/jacoco/test/jacocoTest.exec")
    }
}
