package io.github.surpsg.deltacoverage.report.summary

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.ReportBound
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.paths.shouldNotExist
import org.junit.jupiter.api.Test
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.readText
import kotlin.io.path.writeText


class CoverageCheckSummaryTest {

    private val fileSystem: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    @Test
    fun `should overwrite file with summary`() {
        // GIVEN
        val file: Path = fileSystem.getPath("/summary-2.json").apply {
            createFile().writeText(UNEXPECTED_TEXT)
        }

        val coverageSummary: CoverageSummary = buildSummary()

        // WHEN
        CoverageCheckSummary.create(file, coverageSummary)

        // THEN
        val actualDeserialized: Map<String, Any> = file.readText().deserializeJsonToMap()
        val expected: Map<String, Any> = buildExpectedSummary()
        actualDeserialized.shouldContainAll(expected)
    }

    @Test
    fun `should do nothing if coverage bound is not delta coverage`() {
        // GIVEN
        val file: Path = fileSystem.getPath("/summary-any.json")

        val coverageSummary: CoverageSummary = buildSummary().copy(reportBound = ReportBound.FULL_REPORT)

        // WHEN
        CoverageCheckSummary.create(file, coverageSummary)

        // THEN
        file.shouldNotExist()
    }

    private fun buildSummary() = CoverageSummary(
        view = VIEW_NAME,
        reportBound = ReportBound.DELTA_REPORT,
        coverageRulesConfig = CoverageRulesConfig {
            failOnViolation = true
            violationRules += ViolationRule {
                coverageEntity = COVERAGE_ENTITY
                minCoverageRatio = MIN_COVERAGE_RATIO
                entityCountThreshold = ENTITY_COUNT_THRESHOLD
            }
        },
        verifications = listOf(
            CoverageSummary.VerificationResult(
                coverageEntity = COVERAGE_ENTITY,
                violation = VIOLATION_TEXT,
            )
        ),
        coverageInfo = listOf(
            CoverageSummary.Info(
                coverageEntity = COVERAGE_ENTITY,
                covered = ACTUAL_COVERED,
                total = TOTAL_ENTITIES
            )
        ),
    )

    private fun buildExpectedSummary(): Map<String, Any> {
        return mapOf(
            "view" to VIEW_NAME,
            "verifications" to listOf(
                mapOf(
                    "coverageEntity" to "$COVERAGE_ENTITY",
                    "violation" to VIOLATION_TEXT,
                ),
            ),
            "coverageInfo" to listOf(
                mapOf(
                    "coverageEntity" to "$COVERAGE_ENTITY",
                    "covered" to ACTUAL_COVERED,
                    "total" to TOTAL_ENTITIES,
                    "percents" to ACTUAL_COVERAGE,
                ),
            ),
            "coverageRulesConfig" to linkedMapOf(
                "failOnViolation" to true,
                "entitiesRules" to linkedMapOf(
                    "$COVERAGE_ENTITY" to linkedMapOf(
                        "coverageEntity" to "$COVERAGE_ENTITY",
                        "minCoverageRatio" to MIN_COVERAGE_RATIO,
                        "entityCountThreshold" to ENTITY_COUNT_THRESHOLD,
                    ),
                ),
            )
        )
    }

    private fun String.deserializeJsonToMap(): Map<String, Any> {
        return ObjectMapper().let { mapper ->
            val jsonNode: JsonNode = mapper.readTree(this)
            mapper.convertValue(
                jsonNode,
                object : TypeReference<Map<String, Any>>() {}
            )
        }
    }

    private companion object {
        const val UNEXPECTED_TEXT = "unexpected text"
        const val VIEW_NAME = "view1"
        const val VIOLATION_TEXT = "some violation text"

        val COVERAGE_ENTITY = CoverageEntity.BRANCH
        const val MIN_COVERAGE_RATIO = 0.31
        const val ENTITY_COUNT_THRESHOLD = 23

        const val ACTUAL_COVERED = 45
        const val TOTAL_ENTITIES = 100
        val ACTUAL_COVERAGE = ACTUAL_COVERED.toDouble() / TOTAL_ENTITIES * 100
    }
}

