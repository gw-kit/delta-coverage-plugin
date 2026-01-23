package io.github.surpsg.deltacoverage.cli.config

import io.github.surpsg.deltacoverage.cli.DeltaCoverageCli

internal object CliConfigBuilder{

    fun build(cli: DeltaCoverageCli): CliConfig {
        val baseConfig = cli.configFile?.let { ConfigLoader.loadFromFile(it) } ?: CliConfig()

        return baseConfig.copy(
            coverageEngine = cli.engine ?: baseConfig.coverageEngine,
            viewName = cli.viewName ?: baseConfig.viewName,
            diffSourceFile = cli.diffFile ?: baseConfig.diffSourceFile,
            coverageBinaryFiles = cli.coverageBinaryFiles.ifEmpty { baseConfig.coverageBinaryFiles },
            classRoots = cli.classRoots.ifEmpty { baseConfig.classRoots },
            sourceFiles = cli.sourceFiles.ifEmpty { baseConfig.sourceFiles },
            excludeClasses = cli.excludeClasses.ifEmpty { baseConfig.excludeClasses },
            reports = buildReportsConfig(baseConfig.reports, cli),
            violationRules = buildViolationRules(baseConfig.violationRules, cli)
        )
    }

    private fun buildReportsConfig(
        base: CliConfig.ReportsCliConfig,
        cli: DeltaCoverageCli,
    ): CliConfig.ReportsCliConfig {
        val hasReportFlags = cli.html || cli.xml || cli.console || cli.markdown
        return base.copy(
            reportDir = cli.reportDir ?: base.reportDir,
            html = if (hasReportFlags) cli.html else base.html,
            xml = if (hasReportFlags) cli.xml else base.xml,
            console = if (hasReportFlags) cli.console else base.console,
            markdown = if (hasReportFlags) cli.markdown else base.markdown,
            fullCoverage = if (cli.fullCoverage) true else base.fullCoverage
        )
    }

    private fun buildViolationRules(
        base: CliConfig.ViolationRulesConfig,
        cli: DeltaCoverageCli,
    ): CliConfig.ViolationRulesConfig {
        return base.copy(
            minCoverage = cli.minCoverage ?: base.minCoverage,
            failOnViolation = if (cli.failOnViolation) true else base.failOnViolation
        )
    }
}
