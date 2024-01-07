package io.github.surpsg.deltacoverage.gradle.utils

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

internal inline fun <reified T> ObjectFactory.new(): T = newInstance(T::class.java)

internal fun ObjectFactory.booleanProperty(default: Boolean): Property<Boolean> {
    return property(Boolean::class.javaObjectType).convention(default)
}

internal fun ObjectFactory.doubleProperty(default: Double): Property<Double> {
    return property(Double::class.javaObjectType).convention(default)
}

internal fun ObjectFactory.stringProperty(default: String): Property<String> {
    return property(String::class.javaObjectType).convention(default)
}

internal fun ObjectFactory.stringProperty(default: () -> String): Property<String> {
    return property(String::class.javaObjectType).convention(
        default()
    )
}

internal inline fun <reified K, reified V> ObjectFactory.map(): MapProperty<K, V> =
    mapProperty(K::class.java, V::class.java)
