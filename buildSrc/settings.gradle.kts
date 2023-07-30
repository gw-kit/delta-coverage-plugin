rootProject.name = "delta-coverage-conventions"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
    versionCatalogs {
        create("deps") {
            from(files("../gradle/deps.versions.toml"))
        }
    }
}
