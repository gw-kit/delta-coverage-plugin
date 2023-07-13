package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_TASK
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class CoverageEntityThresholdTest : BaseDeltaCoverageTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle",
        "test.diff.file"
    )

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `delta-coverage should not fail build on printing extension`() {
        // GIVEN
        buildFile.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                println(this)    
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner.runTask(DELTA_COVERAGE_TASK).assertDeltaCoverageStatusEqualsTo(SUCCESS)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "INSTRUCTION, 18",
            "BRANCH, 7",
            "LINE, 7",
        ]
    )
    fun `delta-coverage should ignore low coverage if entity threshold is not met`(
        coverageEntity: String,
        entityCountThreshold: Int
    ) {
        // GIVEN
        buildFile.appendText(
            """

            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                
                violationRules {
                    failOnViolation.set(true)
                    
                    rule(io.github.surpsg.deltacoverage.gradle.CoverageEntity.$coverageEntity) {
                        minCoverageRatio.set(1d)
                        entityCountThreshold.set($entityCountThreshold)
                    }
                }
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner.runTask(DELTA_COVERAGE_TASK)
            .assertDeltaCoverageStatusEqualsTo(SUCCESS)
            .assertOutputContainsStrings("violation", coverageEntity, "ignored")
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "INSTRUCTION, 17",
            "BRANCH, 6",
            "LINE, 6",
        ]
    )
    fun `delta-coverage should fail build if coverage is low and entity count greater or equal to threshold`(
        coverageEntity: CoverageEntity,
        entityCountThreshold: Int,
    ) {
        // GIVEN
        buildFile.appendText(
            """

            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                
                violationRules {
                    failOnViolation.set(true)
                    
                    rule(io.github.surpsg.deltacoverage.gradle.CoverageEntity.${coverageEntity.name}) {
                        minCoverageRatio.set(1d)
                        entityCountThreshold.set($entityCountThreshold)
                    }
                }
            }
        """.trimIndent()
        )

        // WHEN
        val result = gradleRunner.runTaskAndFail(DELTA_COVERAGE_TASK)

        // THEN
        result.assertDeltaCoverageStatusEqualsTo(FAILED)
    }

    @Test
    fun `delta-coverage rules should be configured with single all function`() {
        // GIVEN
        buildFile.delete()
        buildFile = File(rootProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
            import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
            import io.github.surpsg.deltacoverage.gradle.CoverageEntity.*
                
            plugins {
                java
                id("io.github.surpsg.delta-coverage")
            }
            
            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation("junit:junit:4.13.2")
            }

            configure<DeltaCoverageConfiguration> {
                diffSource.file.set("$diffFilePath")
                violationRules {
                    failOnViolation.set(true)
                    all {
                        entityCountThreshold.set(100)
                        minCoverageRatio.set(1.0)
                    }
                }
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runTask(DELTA_COVERAGE_TASK)
            .assertDeltaCoverageStatusEqualsTo(SUCCESS)
            .assertOutputContainsStrings("violation", "ignored", "INSTRUCTION", "BRANCH", "LINE")
    }

    @Test
    fun `delta-coverage rule should be configured with invoke operator`() {
        // GIVEN
        buildFile.delete()
        buildFile = File(rootProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
            import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
            import io.github.surpsg.deltacoverage.gradle.CoverageEntity.*
                
            plugins {
                java
                id("io.github.surpsg.delta-coverage")
            }
            
            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation("junit:junit:4.13.2")
            }

            configure<DeltaCoverageConfiguration> {
                diffSource.file.set("$diffFilePath")   
                violationRules {
                    failIfCoverageLessThan(1.0)
                    INSTRUCTION.invoke { entityCountThreshold.set(1000) }
                    BRANCH { entityCountThreshold.set(1000) }
                    LINE { entityCountThreshold.set(1000) }
                }
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runTask(DELTA_COVERAGE_TASK)
            .assertDeltaCoverageStatusEqualsTo(SUCCESS)
            .assertOutputContainsStrings("violation", "ignored", "INSTRUCTION", "BRANCH", "LINE")
    }
}
