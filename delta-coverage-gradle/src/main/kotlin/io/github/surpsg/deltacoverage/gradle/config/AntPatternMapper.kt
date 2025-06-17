package io.github.surpsg.deltacoverage.gradle.config

internal fun String.antToRegex(): String {
    val regex = StringBuilder("^")

    val antPattern: String = this
    var i = 0

    while (i < antPattern.length) {
        val c = antPattern[i]
        when (c) {
            '*' -> {
                if (i + 1 < antPattern.length && antPattern[i + 1] == '*') {
                    regex.append(".*")
                    i++ // Skip next *
                } else {
                    regex.append("[^/]*")
                }
            }
            '?' -> regex.append("[^/]")
            in "+()^$.{}[]|\\" -> regex.append("\\").append(c)
            else -> regex.append(c)
        }
        i++
    }

    return regex.append("$").toString()
}
