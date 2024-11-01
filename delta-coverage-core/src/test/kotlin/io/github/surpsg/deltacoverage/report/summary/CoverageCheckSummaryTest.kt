package io.github.surpsg.deltacoverage.report.summary

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.report.CoverageVerificationResult
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.readText
import kotlin.io.path.writeText


class CoverageCheckSummaryTest {

    private val fileSystem: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    @Test
    fun `should create file with empty json array`() {
        // GIVEN
        val file: Path = fileSystem.getPath("/summary-1.json")

        // WHEN
        CoverageCheckSummary.create(file, emptyList())

        // THEN
        file.readText() shouldBe "[]"
    }

    @Test
    fun `should overwrite file with summary`() {
        // GIVEN
        val file: Path = fileSystem.getPath("/summary-2.json").apply {
            createFile().writeText(UNEXPECTED_TEXT)
        }

        val verificationResults = listOf(
            CoverageVerificationResult(
                view = VIEW_NAME,
                violations = listOf(VIOLATION_TEXT),
                coverageRulesConfig = CoverageRulesConfig {
                    failOnViolation = true
                    violationRules += ViolationRule {
                        coverageEntity = COVERAGE_ENTITY
                        minCoverageRatio = MIN_COVERAGE_RATIO
                        entityCountThreshold = ENTITY_COUNT_THRESHOLD
                    }
                },
            )
        )

        // WHEN
        CoverageCheckSummary.create(file, verificationResults)

        // THEN
        val actualDeserialized: List<Map<String, Any>> = file.readText().deserializeJsonToMap()
        assertSoftly(actualDeserialized) {
            shouldHaveSize(1)
            first().shouldContainAll(
                mapOf(
                    "view" to VIEW_NAME,
                    "violations" to listOf(VIOLATION_TEXT),
                    "coverageRulesConfig" to linkedMapOf(
                        "failOnViolation" to true,
                        "entitiesRules" to linkedMapOf(
                            "$COVERAGE_ENTITY" to linkedMapOf(
                                "coverageEntity" to "$COVERAGE_ENTITY",
                                "minCoverageRatio" to MIN_COVERAGE_RATIO,
                                "entityCountThreshold" to ENTITY_COUNT_THRESHOLD
                            )
                        ),
                    )
                )
            )
        }
    }

    private fun String.deserializeJsonToMap(): List<Map<String, Any>> {
        return ObjectMapper().let { mapper ->
            val jsonNode: JsonNode = mapper.readTree(this)
            mapper.convertValue(
                jsonNode,
                object : TypeReference<List<Map<String, Any>>>() {}
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
    }
}

