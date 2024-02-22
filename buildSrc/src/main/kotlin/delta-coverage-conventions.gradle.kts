import io.github.surpsg.deltacoverage.CoverageEngine

plugins {
    base
    id("basic-coverage-conventions")
    id("io.github.surpsg.delta-coverage")
}

deltaCoverageReport {
    coverage.engine = CoverageEngine.INTELLIJ

    diffSource {
        git.diffBase = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
    }

    reports {
        html = true
        xml = true
    }

    violationRules.failIfCoverageLessThan(0.9)
}
