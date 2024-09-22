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
        markdown = true
    }

    view(JavaPlugin.TEST_TASK_NAME) {
        violationRules.failIfCoverageLessThan(0.9)
    }
    view("functionalTest") {
        violationRules.failIfCoverageLessThan(0.6)
    }
}
