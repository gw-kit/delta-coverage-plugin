plugins {
    kotlin("jvm")
    `maven-publish`

    id("io.gitlab.arturbosch.detekt")
    id("unit-tests-conventions")

    id("basic-coverage-conventions")
}

val targetJvmVersion = JavaLanguageVersion.of(17)
kotlin {
    jvmToolchain {
        languageVersion.set(targetJvmVersion)
    }
}

java {
    toolchain {
        languageVersion.set(targetJvmVersion)
    }
}

repositories {
    mavenCentral()
}

if (project.hasProperty("snapshotPrefix")) {
    val snapshotPrefix: String by project
    version = "$version-${snapshotPrefix}"
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/gw-kit/delta-coverage-plugin")
            credentials {
                username = System.getenv("GH_USER")
                password = System.getenv("GH_TOKEN")
            }
            mavenContent {
                snapshotsOnly()
            }
        }
    }
}
