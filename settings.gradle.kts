plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "delta-coverage-gradle-plugin"
include("delta-coverage-core")
include("delta-coverage-gradle")

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
        maven {
            url = uri("https://maven.pkg.github.com/gw-kit/cover-jet-plugin")
            credentials {
                username = extra.properties["GH_USER"]?.toString() ?: System.getenv("GH_USER")
                password = extra.properties["GH_TOKEN"]?.toString() ?: System.getenv("GH_TOKEN")
            }
        }
    }
}

buildCache {
    local.isEnabled = true
}
