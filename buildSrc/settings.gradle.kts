rootProject.name = "delta-coverage-conventions"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("deps") {
            from(files("../gradle/deps.versions.toml"))
        }
    }
}
