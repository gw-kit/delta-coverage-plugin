plugins {
    base
    id("delta-coverage-conventions")
    `test-report-aggregation`
    `jacoco-aggregate-conventions`
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
