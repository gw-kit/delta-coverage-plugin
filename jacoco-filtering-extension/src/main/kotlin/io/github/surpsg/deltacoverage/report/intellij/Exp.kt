package io.github.surpsg.deltacoverage.report.intellij

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
import java.io.File

fun main() {
    val config = DeltaCoverageConfig {
        reportName = "Delta Coverage Report"
        diffSourceConfig = DiffSourceConfig { file = "/Users/sergnat/ideaProjects/delta-coverage-plugin/patchik.diff" }
        reportsConfig = ReportsConfig {
            baseReportDir = "/Users/sergnat/ideaProjects/delta-coverage-plugin/build/rep"
            html = ReportConfig { enabled = true; outputFileName = "khtml" }
            xml = ReportConfig { enabled = true; outputFileName = "kxml" }
            csv = ReportConfig { enabled = false; outputFileName = "kcsv" }
        }

        coverageRulesConfig = CoverageRulesConfig {
            failOnViolation = true
            violationRules += ViolationRule {
                minCoverageRatio = 1.0
                coverageEntity = CoverageEntity.LINE
            }
        }
        binaryCoverageFiles += setOf(File("/Users/sergnat/ideaProjects/delta-coverage-plugin/jacoco-filtering-extension/build/kover/bin-reports/test.ic"))
        sourceFiles += setOf(
            File("/Users/sergnat/ideaProjects/delta-coverage-plugin/jacoco-filtering-extension/src/main/kotlin"),
            File("/Users/sergnat/ideaProjects/delta-coverage-plugin/delta-coverage/src/main/kotlin"),
        )
        classFiles += setOf(
            File("/Users/sergnat/ideaProjects/delta-coverage-plugin/jacoco-filtering-extension/build/classes/kotlin/main"),
            File("/Users/sergnat/ideaProjects/delta-coverage-plugin/delta-coverage/build/classes/kotlin/main")
        )
    }
    val baseReportDir = File(config.reportsConfig.baseReportDir)
    DeltaReportFacadeFactory
        .buildFacade(baseReportDir, CoverageEngine.INTELLIJ, config)
        .saveDiffTo(File("./diffchyk.diff"))
        .generateReport()
}
