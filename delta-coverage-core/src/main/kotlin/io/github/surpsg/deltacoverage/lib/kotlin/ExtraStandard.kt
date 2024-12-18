package io.github.surpsg.deltacoverage.lib.kotlin

internal inline fun <T> T.applyIf(condition: Boolean, block: T.() -> T): T = if (condition) {
    block()
} else {
    this
}
