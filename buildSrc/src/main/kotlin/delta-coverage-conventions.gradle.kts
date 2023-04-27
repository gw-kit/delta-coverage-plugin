plugins {
    `base`
    `jacoco`
    id("io.github.surpsg.delta-coverage")
}

val isGithub = project.hasProperty("github")

deltaCoverageReport {
    diffSource {
        val targetBranch = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
        git.diffBase.set(targetBranch)
    }

    if (isGithub) {
        jacocoExecFiles = fileTree("tests-artifacts/") { include("**/*.exec") }
    }

    reports {
        html.set(true)
        xml.set(true)
    }

    violationRules.failIfCoverageLessThan(0.9)
}
