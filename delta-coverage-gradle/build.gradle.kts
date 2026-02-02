plugins {
    `gradle-plugin-conventions`
    `java-test-fixtures`
}

// Create testListener source set for the JUnit Platform TestExecutionListener
val testListenerSourceSet = sourceSets.create("testListener") {
    java.srcDir("src/testListener/kotlin")
    resources.srcDir("src/testListener/resources")
}

// Create testListener JAR task
val testListenerJar by tasks.registering(Jar::class) {
    archiveClassifier.set("test-listener")
    from(testListenerSourceSet.output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Handle duplicates in processTestListenerResources
tasks.named<ProcessResources>("processTestListenerResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Include testListener classes in the main JAR so they are available at runtime
// BUT exclude the ServiceLoader registration - we don't want to auto-register
// the listener when the plugin JAR is on the classpath (e.g., in functional tests)
tasks.named<Jar>("jar") {
    from(testListenerSourceSet.output) {
        exclude("META-INF/services/**")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(tasks.named("testListenerClasses"))
}

// Create configuration for testListener dependencies
val testListenerImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

gradlePlugin {
    website.set("https://github.com/SurpSG/delta-coverage")
    vcsUrl.set("https://github.com/SurpSG/delta-coverage.git")

    plugins {
        create("deltaCoveragePlugin") {
            id = "io.github.gw-kit.delta-coverage"
            displayName = "Delta Coverage"
            description = "Plugin that computes code coverage on modified code"
            implementationClass = "io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin"
            tags.set(listOf("differential", "diff", "delta", "coverage", "jacoco"))
        }
    }
}

dependencies {
    implementation(project(":delta-coverage-core"))
    implementation(deps.coverJetPlugin)

    // testListener source set dependencies
    testListenerImplementation(project(":delta-coverage-core"))
    testListenerImplementation(deps.junitPlatformLauncher)

    testImplementation(gradleApi()) // required to add this dependency explicitly after applying shadowJar plugin
    testImplementation(deps.jimFs)
    testRuntimeOnly(deps.kotlinJvm)

    functionalTestImplementation(project(":delta-coverage-gradle"))
    functionalTestImplementation(testFixtures(project))
    functionalTestImplementation(deps.jgit)
    functionalTestImplementation(deps.gradleProbe)
    functionalTestImplementation(deps.jacksonKotlin)

    testFixturesApi(project(":delta-coverage-core"))
    testFixturesImplementation(deps.kotestAssertions)
    testFixturesImplementation(deps.junitApi)
    testFixturesImplementation(deps.jgit)
    testFixturesImplementation(deps.mockk)
    testFixturesImplementation(deps.gradleProbe)
}

configurations.all {
    resolutionStrategy.force(deps.intellijCoverageAgent)
}
