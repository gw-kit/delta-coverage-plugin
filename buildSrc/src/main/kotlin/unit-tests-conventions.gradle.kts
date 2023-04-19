import io.gradle.surpsg.deltacoverage.libDeps
import org.gradle.kotlin.dsl.`jvm-test-suite`
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.`maven-publish`

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
        }
    }
}
