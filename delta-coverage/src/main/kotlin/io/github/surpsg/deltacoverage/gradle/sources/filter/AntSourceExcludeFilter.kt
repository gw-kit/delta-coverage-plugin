package io.github.surpsg.deltacoverage.gradle.sources.filter

import org.gradle.api.file.FileCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class AntSourceExcludeFilter(
    private val patterns: List<String>
) : SourceFilter {

    override fun filter(inputSource: SourceFilter.InputSource): FileCollection {
        return if (patterns.isEmpty()) {
            inputSource.originSourcesToFilter
        } else {
            log.debug(
                "Applied exclude patterns {} to source: {}",
                patterns,
                inputSource.sourceType.resourceName(inputSource.provider)
            )
            inputSource.originSourcesToFilter.asFileTree.matching { pattern ->
                pattern.exclude(patterns)
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AntSourceExcludeFilter::class.java)
    }
}

