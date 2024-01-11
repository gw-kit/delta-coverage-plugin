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
    dependsOn(
        provider {
            subprojects.map { it.tasks.named("check") }
        }
    )
}
