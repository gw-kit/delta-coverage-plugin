plugins {
    `gradle-plugin-conventions`
}

gradlePlugin {
    website.set("https://gw-kit.github.io/delta-coverage-plugin")
    vcsUrl.set("https://github.com/gw-kit/delta-coverage-plugin.git")
    plugins {
        create("testImpactPlugin") {
            id = "io.github.gw-kit.test-impact"
            displayName = "Test Impact Analysis"
            description = "Sampling-based test-to-code mapping and impact analysis"
            implementationClass = "io.github.gwkit.testimpact.gradle.TestImpactPlugin"
            tags.set(listOf("test", "impact", "mapping", "jfr", "sampling", "performance"))
        }
    }
}

dependencies {
    // Jackson for JSON report output
    implementation(deps.jackson)
    implementation(deps.jacksonKotlin)

    // async-profiler JFR-to-flamegraph converter
    implementation(deps.jfrConverter)

    // Functional tests
    functionalTestImplementation(deps.jacksonKotlin)
    functionalTestImplementation(deps.gradleProbe)
}
