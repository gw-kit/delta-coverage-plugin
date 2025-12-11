plugins {
    `jvm-project-conventions`
    application
    alias(deps.plugins.shadow)
}

dependencies {
    implementation(project(":delta-coverage-core"))
    implementation(deps.jackson)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.20.1")
    implementation(deps.picocli)

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.20")
}

application {
    mainClass.set("io.github.surpsg.deltacoverage.cli.DeltaCoverageCliKt")
}

tasks.shadowJar {
    archiveBaseName.set("delta-coverage-cli")
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "io.github.surpsg.deltacoverage.cli.DeltaCoverageCliKt"
    }
}
