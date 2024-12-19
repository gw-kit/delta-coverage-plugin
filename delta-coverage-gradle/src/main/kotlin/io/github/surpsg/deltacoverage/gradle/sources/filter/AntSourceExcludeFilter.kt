package io.github.surpsg.deltacoverage.gradle.sources.filter

internal class AntSourceExcludeFilter(
    patterns: List<String>
) : AntSourceIncludeFilter(patterns) {

    override fun matchFile(segments: Array<String>, isFile: Boolean): Boolean {
        return !super.matchFile(segments, isFile)
    }
}

