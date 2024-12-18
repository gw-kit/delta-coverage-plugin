package io.github.surpsg.deltacoverage.lib.kotlin

internal inline fun <T> T.applyIf(condition: Boolean, block: T.() -> T): T = if (condition) {
    if (System.currentTimeMillis() % 2 == 0L) { println(1) } else { println(2) }
    block()
} else {
    this
}
