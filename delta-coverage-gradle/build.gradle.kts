plugins {
    `gradle-plugin-conventions`
    `java-test-fixtures`
}

gradlePlugin {
    website.set("https://gw-kit.github.io/delta-coverage-plugin")
    vcsUrl.set("https://github.com/SurpSG/delta-coverage.git")

    plugins {
        create("deltaCoveragePlugin") {
            id = "io.github.gw-kit.delta-coverage"
            displayName = "Delta Coverage"
            description = "Plugin that computes code coverage on modified code"
            implementationClass = "io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin"
            tags.set(listOf("differential", "diff", "delta", "coverage", "jacoco"))
        }
    }
}

dependencies {
    implementation(project(":delta-coverage-core"))
    implementation(deps.coverJetPlugin)

    testImplementation(gradleApi())
    testImplementation(deps.jimFs)
    testRuntimeOnly(deps.kotlinJvm)

    functionalTestImplementation(project(":delta-coverage-gradle"))
    functionalTestImplementation(testFixtures(project))
    functionalTestImplementation(deps.jgit)
    functionalTestImplementation(deps.gradleProbe)
    functionalTestImplementation(deps.jacksonKotlin)

    testFixturesApi(project(":delta-coverage-core"))
    testFixturesImplementation(deps.kotestAssertions)
    testFixturesImplementation(deps.junitApi)
    testFixturesImplementation(deps.jgit)
    testFixturesImplementation(deps.mockk)
    testFixturesImplementation(deps.gradleProbe)
}

configurations.all {
    resolutionStrategy.force(deps.intellijCoverageAgent)
}
