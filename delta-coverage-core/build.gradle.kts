plugins {
    `basic-subproject-conventions`

    signing
}

dependencies {
    implementation(deps.jgit)
    implementation(deps.intellijCoverage)
    implementation(deps.jacocoCore)
    implementation(deps.jacocoReport)
    implementation(deps.httpClient)
    implementation(deps.openCsv)
    implementation(deps.jackson)

    testImplementation(deps.jimFs)
}

publishing {
    publications {
        create<MavenPublication>("MavenCentralPublication") {
            from(components["java"])
            pom {
                name = project.name
                description =
                    "Delta Coverage Core is a library that builds coverage reports for modified and new code in a project."
                url = "https://github.com/gw-kit/delta-coverage-plugin"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                scm {
                    url = "https://github.com/gw-kit/delta-coverage-plugin"
                    connection = "scm:git:git://github.com/gw-kit/delta-coverage-plugin.git"
                    developerConnection = "scm:git:ssh://git@github.com/gw-kit/delta-coverage-plugin.git"
                }
                developers {
                    developer {
                        name = rootProject.findProperty("dev.name")?.toString()
                        email = rootProject.findProperty("dev.email")?.toString()
                        url = rootProject.findProperty("dev.url")?.toString()
                    }
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/gw-kit/delta-coverage-plugin/issues")
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = rootProject.findProperty("mavenCentralPortalUsername")?.toString()
                password = rootProject.findProperty("mavenCentralPortalPassword")?.toString()
            }
        }
    }
}

if (rootProject.hasProperty("ci")) {
    signing {
        // ~/.gradle/gradle.properties
        val keyId = rootProject.findProperty("signing.keyId")?.toString()
        val secretKey = rootProject.findProperty("signing.secretKey")?.toString()
        val signingPassword = rootProject.findProperty("signing.password")?.toString()
        useInMemoryPgpKeys(keyId, secretKey, signingPassword)

        project.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                sign(this)
            }
        }
    }
}
