rootProject.name = "delta-coverage-conventions"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.github.com/gw-kit/delta-coverage-plugin")
            credentials {
                username = System.getenv("GH_USER") ?: extra["GH_USER"].toString()
                password = System.getenv("GH_TOKEN") ?: extra["GH_TOKEN"].toString()
            }
        }
    }
    versionCatalogs {
        create("deps") {
            from(files("../gradle/deps.versions.toml"))
        }
    }
}
