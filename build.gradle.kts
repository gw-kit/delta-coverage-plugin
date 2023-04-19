plugins {
    `base`
    id("diff-coverage-conventions")
    `test-report-aggregation`
    `jacoco-aggregate-conventions`
}
group = "io.github.surpsg.deltacoverage"

repositories {
    mavenCentral()
}

reporting {
    reports {
        val testAggregateTestReport by creating(AggregateTestReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

dependencies {
    allprojects {
        testReportAggregation(this)
    }
}

tasks.named("check") {
    dependsOn(tasks.named("testAggregateTestReport"))
    dependsOn(subprojects.map { it.tasks.named("check") })
}
