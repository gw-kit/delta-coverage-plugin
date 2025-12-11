package io.github.surpsg.deltacoverage.cli.config

import io.github.surpsg.deltacoverage.CoverageEngine

data class CliConfig(
    val coverageEngine: CoverageEngine? = null,
    val viewName: String = "cli",
    val diffSourceFile: String? = null,
    val coverageBinaryFiles: List<String> = emptyList(),
    val classFiles: List<String> = emptyList(),
    val classRoots: List<String> = emptyList(),
    val sourceFiles: List<String> = emptyList(),
    val excludeClasses: List<String> = emptyList(),
    val reports: ReportsCliConfig = ReportsCliConfig(),
    val violationRules: ViolationRulesConfig = ViolationRulesConfig()
)

data class ReportsCliConfig(
    val reportDir: String = "build/reports/delta-coverage",
    val html: Boolean = true,
    val xml: Boolean = false,
    val console: Boolean = true,
    val markdown: Boolean = false,
    val fullCoverage: Boolean = false
)

data class ViolationRulesConfig(
    val minCoverage: Double? = null,
    val failOnViolation: Boolean = false
)
