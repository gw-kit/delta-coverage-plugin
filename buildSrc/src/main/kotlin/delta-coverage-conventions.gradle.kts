import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.CoverageEntity

plugins {
    base
    id("basic-coverage-conventions")
    id("io.github.gw-kit.delta-coverage")
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
        violationRules {
            failIfCoverageLessThan(0.6)
            CoverageEntity.BRANCH {
                minCoverageRatio = 0.5
            }
        }
    }
    view("aggregated") {
        violationRules.failIfCoverageLessThan(0.91)
    }
}
