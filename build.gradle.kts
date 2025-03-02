plugins {
//    `delta-coverage-conventions`
    `test-report-aggregation`
}

dependencies {
    allprojects {
        testReportAggregation(this)
    }
}

//afterEvaluate {
//
//    deltaCoverageReport {
//        view(JavaPlugin.TEST_TASK_NAME) {
//            violationRules.failIfCoverageLessThan(0.9)
//        }
//
//        view("functionalTest") {
//            coverageBinaryFiles = files(
//                project(":delta-coverage-gradle").layout.buildDirectory.file("coverage/functionalTest.ic")
//            )
//            violationRules {
//                failIfCoverageLessThan(0.6)
//                CoverageEntity.BRANCH {
//                    minCoverageRatio = 0.5
//                }
//            }
//        }
//        view("aggregated") {
//            violationRules.failIfCoverageLessThan(0.91)
//        }
//    }
//}
