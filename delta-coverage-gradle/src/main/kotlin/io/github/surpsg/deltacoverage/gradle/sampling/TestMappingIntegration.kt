package io.github.surpsg.deltacoverage.gradle.sampling

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.utils.deltaCoverageConfig
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.slf4j.LoggerFactory

/**
 * Integrates test-to-code mapping with test tasks.
 */
internal object TestMappingIntegration {

    private val log = LoggerFactory.getLogger(TestMappingIntegration::class.java)

    /**
     * System property keys for passing configuration to test JVM.
     */
    object SystemProperties {
        const val ENABLED = "delta.coverage.sampling.enabled"
        const val INTERVAL_MS = "delta.coverage.sampling.intervalMs"
        const val MAX_DEPTH = "delta.coverage.sampling.maxDepth"
        const val EXCLUDE_PREFIXES = "delta.coverage.sampling.excludePrefixes"
        const val OUTPUT_FILE = "delta.coverage.sampling.outputFile"
    }

    /**
     * Configures all test tasks with sampling if enabled.
     */
    fun configure(project: Project) {
        project.afterEvaluate {
            val config = project.deltaCoverageConfig.testMapping
            if (!config.enabled.get()) {
                log.debug("Test mapping is disabled")
                return@afterEvaluate
            }

            log.info("Test mapping is enabled, configuring test tasks")
            configureTestTasks(project, config)
        }
    }

    private fun configureTestTasks(project: Project, config: TestMappingConfiguration) {
        // Configure test tasks in root project
        configureProjectTestTasks(project, config)

        // Configure test tasks in all subprojects
        project.subprojects { subproject ->
            subproject.afterEvaluate {
                configureProjectTestTasks(subproject, config)
            }
        }
    }

    private fun configureProjectTestTasks(project: Project, config: TestMappingConfiguration) {
        project.tasks.withType(Test::class.java).configureEach { testTask ->
            configureTestTask(testTask, config, project)
        }
    }

    private fun configureTestTask(
        testTask: Test,
        config: TestMappingConfiguration,
        project: Project,
    ) {
        log.info("Configuring test task '${testTask.name}' in project '${project.path}' for sampling")

        val samplingConfig = config.sampling
        val outputConfig = config.output

        // Pass configuration via system properties
        testTask.systemProperty(SystemProperties.ENABLED, "true")
        testTask.systemProperty(SystemProperties.INTERVAL_MS, samplingConfig.intervalMs.get().toString())
        testTask.systemProperty(SystemProperties.MAX_DEPTH, samplingConfig.maxDepth.get().toString())
        testTask.systemProperty(
            SystemProperties.EXCLUDE_PREFIXES,
            samplingConfig.excludePackagePrefixes.get().joinToString(",")
        )

        // Resolve output file path relative to project root
        val outputFile = project.rootProject.projectDir.resolve(outputConfig.samplesFile.get())
        testTask.systemProperty(SystemProperties.OUTPUT_FILE, outputFile.absolutePath)

        testTask.jvmArgs(
            "-Djunit.platform.launcher.interceptors.enabled=true"
        )

        // Add test listener to test classpath with ServiceLoader registration
        addTestListenerToClasspath(testTask, project)
    }

    private fun addTestListenerToClasspath(testTask: Test, project: Project) {
        // Find the JARs containing the plugin and core classes first
        // If we can't find them, skip listener registration to avoid ServiceLoader errors
        // We need exactly 2 JARs: one for the listener (gradle module) and one for the sampler (core module)
        val pluginJars = findPluginJars()
        if (pluginJars.size < REQUIRED_JARS_COUNT) {
            log.warn(
                "Could not find all delta-coverage plugin JARs for test mapping (found {} of {} required). " +
                    "Test sampling will be disabled for task '${testTask.name}'. " +
                    "This may happen in test environments like Gradle TestKit.",
                pluginJars.size, REQUIRED_JARS_COUNT
            )
            return
        }

        val listenerConfig = project.configurations.findByName(LISTENER_CONFIGURATION_NAME)
            ?: project.configurations.create(LISTENER_CONFIGURATION_NAME) { config ->
                config.isCanBeConsumed = false
                config.isCanBeResolved = true
            }

        // Add required dependencies for the test listener
        project.dependencies.add(
            LISTENER_CONFIGURATION_NAME,
            "org.junit.platform:junit-platform-launcher:${JUNIT_PLATFORM_VERSION}"
        )

        // Add Jackson dependencies for JSON serialization
        project.dependencies.add(
            LISTENER_CONFIGURATION_NAME,
            "com.fasterxml.jackson.core:jackson-databind:${JACKSON_VERSION}"
        )
        project.dependencies.add(
            LISTENER_CONFIGURATION_NAME,
            "com.fasterxml.jackson.module:jackson-module-kotlin:${JACKSON_VERSION}"
        )

        log.info("Adding {} plugin JARs to test classpath for sampling", pluginJars.size)
        pluginJars.forEach { jar ->
            testTask.classpath += project.files(jar)
        }

        // Create a temp directory with ServiceLoader registration for the listener
        val serviceLoaderDir = createServiceLoaderDir(project)
        testTask.classpath += project.files(serviceLoaderDir)

        testTask.classpath += listenerConfig
    }

    private fun createServiceLoaderDir(project: Project): java.io.File {
        val baseDir = project.layout.buildDirectory.dir("delta-coverage-sampling").get().asFile
        val servicesDir = baseDir.resolve("META-INF/services")
        servicesDir.mkdirs()

        val serviceFile = servicesDir.resolve("org.junit.platform.launcher.TestExecutionListener")
        serviceFile.writeText(SAMPLING_LISTENER_CLASS)

        return baseDir
    }

    private fun findPluginJars(): List<java.io.File> {
        val jars = mutableListOf<java.io.File>()

        // Find the JARs containing the plugin and core classes
        // We use classes that don't have external dependencies to avoid NoClassDefFoundError
        // - TestMappingIntegration is in the gradle plugin JAR (same JAR as SamplingTestListener)
        // - StackSampler is in the core JAR
        val classesToFind = listOf(
            TestMappingIntegration::class.java,  // gradle plugin JAR
            io.github.surpsg.deltacoverage.sampling.StackSampler::class.java,  // core JAR
        )

        for (cls in classesToFind) {
            try {
                val location = cls.protectionDomain?.codeSource?.location
                if (location != null) {
                    val file = java.io.File(location.toURI())
                    // Only accept actual JAR files, not directories or other paths
                    // In TestKit/special classloader environments, the path might be a directory
                    if (file.exists() && file.isFile && file.name.endsWith(".jar") && file !in jars) {
                        log.debug("Found JAR for {}: {}", cls.name, file.absolutePath)
                        jars.add(file)
                    } else {
                        log.debug(
                            "Skipping invalid path for {}: {} (exists={}, isFile={}, isJar={})",
                            cls.name, file.absolutePath, file.exists(), file.isFile, file.name.endsWith(".jar")
                        )
                    }
                } else {
                    log.warn("No code source location for class {}", cls.name)
                }
            } catch (e: Exception) {
                log.warn("Could not find JAR for class {}: {}", cls.name, e.message)
            }
        }

        return jars
    }

    private const val LISTENER_CONFIGURATION_NAME = "deltaCoverageSamplingListener"
    private const val JUNIT_PLATFORM_VERSION = "1.11.4"
    private const val JACKSON_VERSION = "2.21.0"
    private const val SAMPLING_LISTENER_CLASS = "io.github.surpsg.deltacoverage.gradle.sampling.listener.SamplingTestListener"
    private const val REQUIRED_JARS_COUNT = 2
}
