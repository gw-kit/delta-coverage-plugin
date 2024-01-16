import io.gradle.surpsg.deltacoverage.libDeps

plugins {
    kotlin("jvm")

    `jvm-test-suite`
}


testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libDeps.assertj)
                implementation(libDeps.mockk)
                implementation(libDeps.kotestRunnerJunit5)
                implementation(libDeps.kotestAssertions)
                implementation(libDeps.kotestProperty)
            }
            targets.all {
                testTask.configure {
                    testLogging.showStandardStreams = true
                }
            }
        }
    }
}
