import io.gradle.surpsg.deltacoverage.libDeps
import org.gradle.kotlin.dsl.base
import org.gradle.kotlin.dsl.`jvm-test-suite`

plugins {
    base
    kotlin("jvm")
    `jvm-test-suite`
    `jacoco`
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

                maxParallelForks = 4
                setForkEvery(1L)

                systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_method")
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
