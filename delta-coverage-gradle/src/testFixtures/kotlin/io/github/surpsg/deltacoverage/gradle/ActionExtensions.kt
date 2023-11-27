package org.gradle.kotlin.dsl

import org.gradle.api.Action

operator fun <T> Action<in T>.invoke(target: T) = execute(target)
