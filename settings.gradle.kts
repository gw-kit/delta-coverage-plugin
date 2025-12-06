plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "delta-coverage-gradle-plugin"
include("delta-coverage-core")
include("delta-coverage-gradle")
include("delta-coverage-demo")

dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            from(files("gradle/deps.versions.toml"))
        }
    }
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()

        val ghUser = extra.properties["GH_USER"]?.toString() ?: System.getenv("GH_USER")
        val ghPass = extra.properties["GH_TOKEN"]?.toString() ?: System.getenv("GH_TOKEN")
        maven {
            url = uri("https://maven.pkg.github.com/gw-kit/cover-jet-plugin")
            credentials {
                username = ghUser
                password = ghPass
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/gw-kit/gradle-probe")
            credentials {
                username = ghUser
                password = ghPass
            }
        }
    }
}

buildCache {
    local.isEnabled = true
}
