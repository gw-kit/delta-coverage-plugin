package io.github.surpsg.deltacoverage.incubator

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.Reporter
import com.intellij.rt.coverage.report.data.BinaryReport
import com.intellij.rt.coverage.report.data.Module
import io.github.surpsg.deltacoverage.config.*
import io.github.surpsg.deltacoverage.diff.CodeUpdateInfo
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.diff.diffSourceFactory
import io.github.surpsg.deltacoverage.diff.parse.ModifiedLinesDiffParser
import java.io.File

fun main() {
    val config = DeltaCoverageConfig {
        reportName = "Delta Coverage Report"
        diffSourceConfig = DiffSourceConfig {
            file = "/home/sergnat/ideaProjects/delta-coverage-gradle/diff.patch"
        }
        reportsConfig = ReportsConfig {
            baseReportDir = "/home/sergnat/ideaProjects/delta-coverage-gradle/build/rep"
            html = ReportConfig {
                enabled = true
                outputFileName = "khtml"
            }
            xml = ReportConfig {
                enabled = true
                outputFileName = "kxml"
            }
            csv = ReportConfig { enabled = true; outputFileName = "kcsv" }
        }

        coverageRulesConfig = CoverageRulesConfig {
            violationRules += ViolationRule {
                coverageEntity = CoverageEntity.INSTRUCTION
                minCoverageRatio = 0.5
                entityCountThreshold = 10
            }
        }
        binaryCoverageFiles += setOf(File("/home/sergnat/ideaProjects/delta-coverage-gradle/jacoco-filtering-extension/build/kover/raw-reports/test.ic"))
        sourceFiles += setOf(
                File("/home/sergnat/ideaProjects/delta-coverage-gradle/jacoco-filtering-extension/src/main/kotlin"),
                File("/home/sergnat/ideaProjects/delta-coverage-gradle/delta-coverage/src/main/kotlin"),
        )
        classFiles += setOf(
                File("/home/sergnat/ideaProjects/delta-coverage-gradle/jacoco-filtering-extension/build/classes/kotlin/main"),
                File("/home/sergnat/ideaProjects/delta-coverage-gradle/delta-coverage/build/classes/kotlin/main")
        )
    }

    generateReport(config)
}

fun generateReport(deltaCoverageConfig: DeltaCoverageConfig) {
    val binaryReports: List<BinaryReport> = deltaCoverageConfig.binaryCoverageFiles.map { binaryCoverageFile ->
        BinaryReport(
                binaryCoverageFile,
                null
        )
    }

    val modules: List<Module> = listOf(
            Module(
                    deltaCoverageConfig.classFiles.toList(),
                    deltaCoverageConfig.sourceFiles.toList()
            )
    )

    val baseReportDir = File(deltaCoverageConfig.reportsConfig.baseReportDir)

    val codeUpdateInfo: CodeUpdateInfo = obtainCodeUpdateInfo(baseReportDir, deltaCoverageConfig)

    val filterProjectData: ProjectData = getProjectData(
            binaryReports,
            modules,
            codeUpdateInfo
    )
    val deltaReportLoadStrategy = DeltaReportLoadStrategy(filterProjectData, binaryReports, modules)
    Reporter(
            deltaReportLoadStrategy
    ).createHTMLReport(
            baseReportDir.resolve(deltaCoverageConfig.reportsConfig.html.outputFileName),
            deltaCoverageConfig.reportName,
            null
    )
}

private fun obtainCodeUpdateInfo(
        baseReportDir: File,
        deltaCoverageConfig: DeltaCoverageConfig
): CodeUpdateInfo {
    val diffSource: DiffSource = diffSourceFactory(baseReportDir, deltaCoverageConfig.diffSourceConfig)
    val modifiedLines: Map<String, Set<Int>> = ModifiedLinesDiffParser().collectModifiedLines(diffSource.pullDiff())
    return CodeUpdateInfo(modifiedLines)
}

class DeltaReportLoadStrategy(
        private val filteredProjectData: ProjectData,
        binaryReports: List<BinaryReport>,
        modules: List<Module>
) : ReportLoadStrategy(binaryReports, modules) {

    override fun loadProjectData(): ProjectData = filteredProjectData
}
