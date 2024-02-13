import io.gradle.surpsg.deltacoverage.libDeps
import org.gradle.kotlin.dsl.base
import org.gradle.kotlin.dsl.`jvm-test-suite`

plugins {
    base
    kotlin("jvm")
    `jvm-test-suite`
    id("basic-coverage-conventions")
}

testing.suites {

    val functionalTest by registering(JvmTestSuite::class) {
        useJUnitJupiter()
        testType.set(TestSuiteType.FUNCTIONAL_TEST)

        sources {
            java {
                setSrcDirs(listOf("src/functionalTest/kotlin"))
            }
        }

        dependencies {
            implementation(project())

            implementation(libDeps.assertj)
            implementation(libDeps.mockk)
            implementation(libDeps.kotestRunnerJunit5)
            implementation(libDeps.kotestAssertions)
            implementation(libDeps.kotestProperty)
        }

        targets.all {
            testTask.configure {
                description = "Runs the functional tests."
                group = "verification"

                testLogging.showStandardStreams = true
                maxParallelForks = 4

                systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("functionalTest"))
}

tasks.jacocoTestReport {
    executionData.setFrom(fileTree(buildDir).include("/jacoco/*.exec"))
}
