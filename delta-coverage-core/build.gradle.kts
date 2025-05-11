import org.gradle.internal.hash.ChecksumService

plugins {
    `jvm-project-conventions`

    `java-library`
    `maven-publish`
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

configurations.all {
    resolutionStrategy.force(deps.intellijCoverageAgent)
}

java {
    withJavadocJar()
    withSourcesJar()
}

val genHashSum = tasks.register<CheckSum>("genHashSum") {
    filesToHash.from(tasks.withType<Jar>())
    filesToHash.from(tasks.withType<GenerateMavenPom>())
}

publishing {
    publications {
        create<MavenPublication>("MavenCentral") {
            from(components["java"])
            genHashSum.configure {
                checkSumFiles.forEach { checkSumFile ->
                    artifact(
                        mapOf(
                            "source" to checkSumFile,
                            "classifier" to checkSumFile.nameWithoutExtension,
                        )
                    )
                }
            }
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
                developers {
                    developer {
                        name = rootProject.findProperty("dev.name")?.toString()
                        email = rootProject.findProperty("dev.email")?.toString()
                        url = rootProject.findProperty("dev.url")?.toString()
                    }
                }
                scm {
                    url = "https://github.com/gw-kit/delta-coverage-plugin"
                    connection = "scm:git:git://github.com/gw-kit/delta-coverage-plugin.git"
                    developerConnection = "scm:git:ssh://git@github.com/gw-kit/delta-coverage-plugin.git"
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
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = rootProject.findProperty("central.user")?.toString()
                password = rootProject.findProperty("central.password")?.toString()
            }
        }
    }
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn(genHashSum)
}

abstract class CheckSum @Inject constructor(
    private val checksumService: ChecksumService,
) : DefaultTask() {

    private val hashAlgorithms = listOf("md5", "sha1")

    @InputFiles
    val filesToHash: ConfigurableFileCollection = project.objects.fileCollection()

    @OutputFiles
    val checkSumFiles: ConfigurableFileCollection = project.files(
        project.provider {
            filesToHash.files.flatMap { file ->
                hashAlgorithms.map { hashAlgorithm ->
                    file.resolveHashSumFile(hashAlgorithm)
                }
            }
        }
    )

    @TaskAction
    fun hashSum() {
        filesToHash.forEach { file: File ->
            hashAlgorithms.forEach { hashAlgorithm ->
                val hashSumFile = file.resolveHashSumFile(hashAlgorithm)
                val hashSum = checksumService.hash(file, hashAlgorithm).toString()
                hashSumFile.writeText(hashSum)
            }
        }
    }

    private fun File.resolveHashSumFile(algorithm: String): File {
        return File("${this.absolutePath}.${algorithm}")
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

tasks.withType<Sign> {
    mustRunAfter(genHashSum)
}
