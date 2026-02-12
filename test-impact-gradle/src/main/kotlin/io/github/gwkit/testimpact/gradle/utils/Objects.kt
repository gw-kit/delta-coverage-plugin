package io.github.gwkit.testimpact.gradle.utils

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

internal inline fun <reified T : Any> ObjectFactory.new(): T = newInstance(T::class.java)

internal fun ObjectFactory.booleanProperty(default: Boolean): Property<Boolean> {
    return property(Boolean::class.javaObjectType).convention(default)
}
