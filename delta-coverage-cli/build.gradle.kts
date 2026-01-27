plugins {
    `jvm-project-conventions`
    `functional-tests-conventions`
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

    functionalTestImplementation(deps.kotestAssertions)
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

    functionalTest {
        inputs.files(shadowJar)
        systemProperty(
            "cli.jar.path",
            shadowJar.get().archiveFile.get().asFile.absolutePath,
        )
    }
}

detekt {
    ignoreFailures = true
}
