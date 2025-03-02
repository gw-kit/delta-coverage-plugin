plugins {
    base
//    `delta-coverage-conventions`
    `test-report-aggregation`
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

//deltaCoverageReport {
//    view("functionalTest") {
//        // TODO it's temporary solution
//        coverageBinaryFiles = files(
//            project(":delta-coverage-gradle").layout.buildDirectory.file("coverage/functionalTest.ic")
//        )
//    }
//}
