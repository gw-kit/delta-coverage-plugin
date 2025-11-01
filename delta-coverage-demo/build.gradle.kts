plugins {
    `jvm-project-conventions`
    application
}

dependencies {
    implementation(project(":delta-coverage-core"))
    implementation(deps.jackson)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.20.1")

    // SLF4J logging
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.20")
}

application {
    mainClass.set("io.github.surpsg.deltacoverage.demo.DeltaCoverageDemoKt")
}

// Exclude this module from publishing
tasks.withType<PublishToMavenRepository>().configureEach {
    enabled = false
}
