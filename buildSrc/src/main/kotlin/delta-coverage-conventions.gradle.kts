import io.github.surpsg.deltacoverage.CoverageEngine

plugins {
    base
    id("basic-coverage-conventions")
    id("io.github.surpsg.delta-coverage")
}

deltaCoverageReport {
    coverage.engine = CoverageEngine.INTELLIJ

    diffSource.byGit {
        diffBase = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
        useNativeGit = true
    }

    reports {
        html = true
        xml = true
        console = true
    }

    violationRules.failIfCoverageLessThan(0.9)
}

tasks.named("gitDiff") {
    outputs.upToDateWhen { false } // TODO: remove this after migration to the next release after 2.2.0
}
