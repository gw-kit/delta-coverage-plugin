import io.gradle.surpsg.deltacoverage.libDeps
import org.gradle.api.tasks.testing.logging.TestLogEvent
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
                outputs.apply {
                    upToDateWhen { false }
                    cacheIf { false }
                }

                description = "Runs the functional tests."
                group = "verification"

                maxParallelForks = 4

                systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
                systemProperty("kotest.framework.classpath.scanning.config.disable", "true")

                testLogging {
                    events(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
                    showStandardStreams = true
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("functionalTest"))
}
