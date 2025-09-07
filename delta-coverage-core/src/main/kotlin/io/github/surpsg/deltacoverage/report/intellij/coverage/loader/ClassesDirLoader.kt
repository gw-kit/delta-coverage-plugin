package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import java.io.File
import java.util.Deque
import java.util.LinkedList

internal class ClassesDirLoader(excludePatterns: Set<String>) {

    private val excludeRegexes: List<Regex> = excludePatterns.map { it.toRegex() }

    fun traverseClasses(rootClassesDir: File): Sequence<JvmClassDesc> {
        if (!shouldInclude(rootClassesDir)) {
            return emptySequence()
        }

        return sequence {
            val traverseQueue: Deque<TraverseCandidate> = LinkedList()
            traverseQueue += collectEntriesFromDir("", rootClassesDir)

            while (traverseQueue.isNotEmpty()) {
                val candidate: TraverseCandidate = traverseQueue.pollFirst()!!
                if (candidate.file.isFile) {
                    if (shouldExclude(candidate.file)) {
                        continue
                    }
                    val className = candidate.resolveClassName()
                    yield(JvmClassDesc(className, candidate.file))
                } else {
                    traverseQueue += collectEntriesFromDir(candidate.resolvePrefixFromThis(), candidate.file)
                }
            }
        }
    }

    private fun collectEntriesFromDir(prefix: String, dir: File): Sequence<TraverseCandidate> =
        (dir.listFiles()?.asSequence() ?: sequenceOf())
            .filter(::shouldInclude)
            .sortedWith { file1, file2 ->
                file1.nameWithoutExtension.compareTo(file2.nameWithoutExtension)
            }
            .map { file ->
                TraverseCandidate(prefix, file)
            }

    private fun shouldInclude(file: File): Boolean = when {
        file.isDirectory -> true
        file.extension == "class" -> true
        else -> false
    }

    private fun shouldExclude(file: File): Boolean = excludeRegexes.any {
        it.matches(file.absolutePath)
    }

    data class TraverseCandidate(val prefix: String, val file: File) {

        fun resolveClassName(): String = if (prefix.isEmpty()) {
            file.nameWithoutExtension
        } else {
            "$prefix.${file.nameWithoutExtension}"
        }

        fun resolvePrefixFromThis(): String = resolveClassName()
    }
}
