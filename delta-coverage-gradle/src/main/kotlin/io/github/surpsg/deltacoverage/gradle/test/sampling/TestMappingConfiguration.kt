package io.github.surpsg.deltacoverage.gradle.test.sampling

import io.github.surpsg.deltacoverage.gradle.utils.booleanProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import javax.inject.Inject

/**
 * Configuration for test-to-code mapping via JFR stack sampling.
 *
 * Example usage:
 * ```kotlin
 * deltaCoverageReport {
 *     testMapping {
 *         enabled = true
 *         includePackages.set(listOf("com.example"))
 *         excludePackages.addAll("org.springframework", "com.fasterxml")
 *     }
 * }
 * ```
 */
open class TestMappingConfiguration @Inject constructor(
    objectFactory: ObjectFactory,
) {
    /**
     * Enables or disables test-to-code mapping.
     * Defaults to false.
     */
    @Input
    val enabled: Property<Boolean> = objectFactory.booleanProperty(false)

    /**
     * Package prefixes to include in the mapping.
     * If empty, all packages are included (except those in excludePackages).
     * Example: ["com.example", "org.mycompany"]
     */
    @Input
    val includePackages: ListProperty<String> = objectFactory.listProperty(String::class.java)
        .convention(emptyList())

    /**
     * Additional package prefixes to exclude from the mapping.
     * These are added to the default excludes (JUnit, Gradle, JDK internals).
     * Example: ["org.springframework", "com.fasterxml"]
     */
    @Input
    val excludePackages: ListProperty<String> = objectFactory.listProperty(String::class.java)
        .convention(emptyList())
}
