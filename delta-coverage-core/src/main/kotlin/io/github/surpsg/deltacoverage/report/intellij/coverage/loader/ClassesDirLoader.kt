package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import java.nio.file.Path
import java.util.Deque
import java.util.LinkedList
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

internal class ClassesDirLoader(
    classesToKeep: Set<Path>,
    excludePatterns: Set<String>,
) {

    private val excludeRegexes: List<Regex> = excludePatterns.map { it.toRegex() }
    private val includeClassFilesPaths: Set<String> =
        classesToKeep.asSequence().filter { it.isRegularFile() }.map { it.absolutePathString() }.toSet()

    fun traverseClasses(rootClassesDir: Path): Sequence<JvmClassDesc> {
        if (shouldKeep(rootClassesDir).not()) {
            return emptySequence()
        }

        return sequence {
            val traverseQueue: Deque<TraverseCandidate> = LinkedList()
            traverseQueue += collectEntriesFromDir("", rootClassesDir)

            while (traverseQueue.isNotEmpty()) {
                val candidate: TraverseCandidate = traverseQueue.pollFirst()!!
                if (candidate.file.isRegularFile()) {
                    val className = candidate.resolveClassName()
                    yield(JvmClassDesc(className, candidate.file))
                } else {
                    traverseQueue += collectEntriesFromDir(candidate.resolvePrefixFromThis(), candidate.file)
                }
            }
        }
    }

    private fun collectEntriesFromDir(prefix: String, dir: Path): Sequence<TraverseCandidate> =
        dir.listDirectoryEntries().asSequence()
            .filter(::shouldKeep)
            .sortedWith { path1, path2 ->
                path1.nameWithoutExtension.compareTo(path2.nameWithoutExtension)
            }
            .map { path ->
                TraverseCandidate(prefix, path)
            }

    private fun shouldKeep(file: Path): Boolean {
        val excludePredicate: Path.() -> Boolean = {
            includeClassFilesPaths.isEmpty() && excludeRegexes.any { it.matches(absolutePathString()) }
        }
        val includePredicate: Path.() -> Boolean = {
            includeClassFilesPaths.isEmpty() || includeClassFilesPaths.contains(absolutePathString())
        }
        return when {
            file.isDirectory() -> !file.excludePredicate()
            file.extension == "class" -> file.includePredicate() && !file.excludePredicate()
            else -> false
        }
    }

    private data class TraverseCandidate(val prefix: String, val file: Path) {

        fun resolveClassName(): String = if (prefix.isEmpty()) {
            file.nameWithoutExtension
        } else {
            "$prefix.${file.nameWithoutExtension}"
        }

        fun resolvePrefixFromThis(): String = resolveClassName()
    }
}
