import io.github.surpsg.deltacoverage.gradle.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.CoverageEntity

plugins {
    id("io.github.gw-kit.delta-coverage")
}

deltaCoverageReport {
    coverage {
        engine = CoverageEngine.INTELLIJ
        autoApplyPlugin = false
    }

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
        violationRules{
            failIfCoverageLessThan(0.91)
            CoverageEntity.BRANCH {
                minCoverageRatio = 0.9
            }
        }
    }
}
