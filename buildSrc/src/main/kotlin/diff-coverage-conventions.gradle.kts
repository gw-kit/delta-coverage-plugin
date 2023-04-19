plugins {
    `base`
    `jacoco`
    id("com.form.diff-coverage")
}

val isGithub = project.hasProperty("github")

diffCoverageReport {
    diffSource {
        git.diffBase = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
    }

    if (isGithub) {
        jacocoExecFiles = fileTree("tests-artifacts/") { include("**/*.exec") }
    }

    reports {
        html = true
        xml = true
    }

    violationRules.failIfCoverageLessThan(0.9)
}
