plugins {
    `jvm-project-conventions`
    application
}

dependencies {
    implementation(project(":delta-coverage-core"))
    implementation(deps.jackson)
    implementation(deps.jacksonKotlin)
    implementation(deps.jacksonYaml)

    // SLF4J logging
    implementation(deps.slf4j)
    implementation(deps.logback)
}

application {
    mainClass.set("io.github.surpsg.deltacoverage.demo.DeltaCoverageDemoKt")
}

// Exclude this module from publishing
tasks.withType<PublishToMavenRepository>().configureEach {
    enabled = false
}
