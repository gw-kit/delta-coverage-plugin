plugins {
    `maven-publish`
}

if (rootProject.hasProperty("snapshotPrefix")) {
    version = "$version-${findProperty("snapshotPrefix")}"
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
