import io.gradle.surpsg.deltacoverage.libDeps
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm")

    `jvm-test-suite`
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
            dependencies {
                implementation(libDeps.mockk)
                implementation(libDeps.kotestAssertions)
            }
            targets.all {
                testTask.configure {
                    outputs.apply {
                        upToDateWhen { false }
                        cacheIf { false }
                    }

                    systemProperty("junit.jupiter.execution.parallel.enabled", true)
                    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
                    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
                    systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
                    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", 2)
                    systemProperty("kotest.framework.classpath.scanning.config.disable", "true")

                    testLogging {
                        events(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
                        showStandardStreams = true
                    }
                }
            }
        }
    }
}
