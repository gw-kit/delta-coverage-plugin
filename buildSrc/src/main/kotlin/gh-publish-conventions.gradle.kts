plugins {
    `maven-publish`
}

if (rootProject.hasProperty("snapshotPrefix")) {
    val snapshotPrefix: String by project
    version = "$version-${snapshotPrefix}"
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "GhPackages"
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
