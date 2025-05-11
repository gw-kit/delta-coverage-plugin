rootProject.name = "delta-coverage-conventions"

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()

        sequenceOf(
            "https://maven.pkg.github.com/gw-kit/delta-coverage-plugin",
            "https://maven.pkg.github.com/gw-kit/cover-jet-plugin",
        ).forEach { ghUrl ->
            maven {
                url = uri(ghUrl)
                credentials {
                    username = extra.properties["GH_USER"]?.toString() ?: System.getenv("GH_USER")
                    password = extra.properties["GH_TOKEN"]?.toString() ?: System.getenv("GH_TOKEN")
                }
            }
        }
    }
    versionCatalogs {
        create("deps") {
            from(files("../gradle/deps.versions.toml"))
        }
    }
}
