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
    val basicDir = "/home/sergnat/ideaProjects/delta-coverage-gradle"
    val config = DeltaCoverageConfig {
        reportName = "Delta Coverage Report"
        diffSourceConfig = DiffSourceConfig { file = "$basicDir/diff.patch" }
        reportsConfig = ReportsConfig {
            baseReportDir = "$basicDir/build/delta-coverage-reports/exp"
            html = ReportConfig { enabled = true; outputFileName = "khtml" }
            xml = ReportConfig { enabled = true; outputFileName = "kxml" }
            csv = ReportConfig { enabled = false; outputFileName = "kcsv" }
        }

        coverageRulesConfig = CoverageRulesConfig {
            failOnViolation = true
            violationRules += ViolationRule {
                minCoverageRatio = 1.0
                coverageEntity = CoverageEntity.LINE
                entityCountThreshold = 6
            }
            violationRules += ViolationRule {
                minCoverageRatio = 1.0
                coverageEntity = CoverageEntity.INSTRUCTION
                entityCountThreshold = 40
            }
            violationRules += ViolationRule {
                minCoverageRatio = 1.0
                coverageEntity = CoverageEntity.BRANCH
                entityCountThreshold = 7
            }
        }
        binaryCoverageFiles += setOf(File("$basicDir/jacoco-filtering-extension/build/kover/raw-reports/test.ic"))
        sourceFiles += setOf(
            File("$basicDir/jacoco-filtering-extension/src/main/kotlin"),
            File("$basicDir/delta-coverage/src/main/kotlin"),
        )
        classFiles += setOf(
            File("$basicDir/jacoco-filtering-extension/build/classes/kotlin/main"),
            File("$basicDir/delta-coverage/build/classes/kotlin/main")
        )
    }
    val baseReportDir = File(config.reportsConfig.baseReportDir)
    DeltaReportFacadeFactory
        .buildFacade(baseReportDir, CoverageEngine.INTELLIJ, config)
        .saveDiffTo(File("./diffchyk.diff"))
        .generateReport()
}
