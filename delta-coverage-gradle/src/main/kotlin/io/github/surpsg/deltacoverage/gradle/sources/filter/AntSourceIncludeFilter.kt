package io.github.surpsg.deltacoverage.gradle.sources.filter

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.pattern.PatternMatcher
import org.gradle.api.internal.file.pattern.PatternMatcherFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

internal open class AntSourceIncludeFilter(
    private val patterns: List<String>
) : SourceFilter {

    private val matcher: PatternMatcher = PatternMatcherFactory.getPatternsMatcher(true, false, patterns)

    override fun filter(inputSource: SourceFilter.InputSource): FileCollection {
        return if (patterns.isEmpty()) {
            inputSource.originSources
        } else {
            log.info(
                "Applied patterns {} to source: {}",
                patterns,
                inputSource.sourceType.resourceName(inputSource.provider)
            )
            filterCollectionFiles(inputSource.originSources)
        }
    }

    internal open fun matchFile(segments: Array<String>, isFile: Boolean): Boolean {
        return matcher.test(segments, isFile)
    }

    private fun filterCollectionFiles(originSources: FileCollection): FileCollection {
        return originSources.asFileTree
            .filter { file ->
                val segments: Array<String> = file.obtainSegments()
                matchFile(segments, file.isFile)
            }
    }

    private fun File.obtainSegments(): Array<String> {
        return this.toPath()
            .toAbsolutePath()
            .asSequence()
            .map { it.toString() }
            .toList()
            .toTypedArray()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AntSourceIncludeFilter::class.java)
    }
}

