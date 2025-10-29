package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import java.io.File
import java.util.Deque
import java.util.LinkedList

internal class ClassesDirLoader(
    classesToKeep: Set<File>,
    excludePatterns: Set<String>,
) {

    private val excludeRegexes: List<Regex> = excludePatterns.map { it.toRegex() }
    private val includeClassFilesPaths: Set<String> =
        classesToKeep.asSequence().filter { it.isFile }.map { it.absolutePath }.toSet()

    fun traverseClasses(rootClassesDir: File): Sequence<JvmClassDesc> {
        if (shouldKeep(rootClassesDir).not()) {
            return emptySequence()
        }

        return sequence {
            val traverseQueue: Deque<TraverseCandidate> = LinkedList()
            traverseQueue += collectEntriesFromDir("", rootClassesDir)

            while (traverseQueue.isNotEmpty()) {
                val candidate: TraverseCandidate = traverseQueue.pollFirst()!!
                if (candidate.file.isFile) {
                    yieldClassFile(candidate)
                } else {
                    traverseQueue += collectEntriesFromDir(candidate.resolvePrefixFromThis(), candidate.file)
                }
            }
        }
    }

    private suspend fun SequenceScope<JvmClassDesc>.yieldClassFile(
        candidate: TraverseCandidate
    ) {
        if (shouldKeep(candidate.file)) {
            val className = candidate.resolveClassName()
            yield(JvmClassDesc(className, candidate.file))
        }
    }

    private fun collectEntriesFromDir(prefix: String, dir: File): Sequence<TraverseCandidate> =
        (dir.listFiles()?.asSequence().orEmpty())
            .filter(::shouldKeep)
            .sortedWith { file1, file2 ->
                file1.nameWithoutExtension.compareTo(file2.nameWithoutExtension)
            }
            .map { file ->
                TraverseCandidate(prefix, file)
            }

    private fun shouldKeep(file: File): Boolean {
        val excludePredicate: File.() -> Boolean = { excludeRegexes.any { it.matches(absolutePath) } }
        val includePredicate: File.() -> Boolean = {
            includeClassFilesPaths.isEmpty() || includeClassFilesPaths.contains(absolutePath)
        }
        return when {
            file.isDirectory -> !file.excludePredicate()
            file.extension == "class" -> file.includePredicate() && !file.excludePredicate()
            else -> false
        }
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
