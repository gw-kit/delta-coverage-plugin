package io.github.surpsg.deltacoverage.gradle.sampling

import io.github.surpsg.deltacoverage.gradle.utils.booleanProperty
import org.gradle.api.model.ObjectFactory
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
}
