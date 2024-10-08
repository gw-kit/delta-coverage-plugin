rootProject.name = "delta-coverage-conventions"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.github.com/gw-kit/delta-coverage-plugin")
            credentials {
                username = extra.properties["GH_USER"]?.toString() ?: System.getenv("GH_USER")
                password = extra.properties["GH_TOKEN"]?.toString() ?: System.getenv("GH_TOKEN")
            }
        }
    }
    versionCatalogs {
        create("deps") {
            from(files("../gradle/deps.versions.toml"))
        }
    }
}
