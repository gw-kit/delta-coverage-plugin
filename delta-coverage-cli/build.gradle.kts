plugins {
    `jvm-project-conventions`
    alias(deps.plugins.shadow)
    alias(deps.plugins.mavenPublish)
}

dependencies {
    implementation(project(":delta-coverage-core"))
    implementation(deps.jackson)
    implementation(deps.jacksonKotlin)
    implementation(deps.jacksonYaml)
    implementation(deps.picocli)

    implementation(deps.slf4j)
    implementation(deps.logback)

    testImplementation(deps.jimFs)
}

tasks {
    jar {
        archiveClassifier.set("plain")
    }

    shadowJar {
        archiveBaseName.set("delta-coverage-cli")
        archiveClassifier.set("")
        manifest {
            attributes["Main-Class"] = "io.github.surpsg.deltacoverage.cli.DeltaCoverageCliKt"
        }
    }
}

detekt {
    ignoreFailures = true
}
