import io.github.surpsg.deltacoverage.CoverageEngine

plugins {
    base
    id("basic-coverage-conventions")
    id("io.github.surpsg.delta-coverage")
}

val isGithub = project.hasProperty("github")

deltaCoverageReport {
    coverage.engine = CoverageEngine.JACOCO

    diffSource {
        val targetBranch = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
        git.diffBase.set(targetBranch)
    }

    if (isGithub) {
        coverageBinaryFiles = fileTree("tests-artifacts/") { include("**/*.exec") }
    }

    reports {
        html.set(true)
        xml.set(true)
    }

    violationRules.failIfCoverageLessThan(0.9)
}
