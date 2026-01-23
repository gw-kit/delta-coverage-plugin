package io.github.surpsg.deltacoverage.cli.config

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

internal class GlobExpander(
    private val fileSystem: FileSystem = FileSystems.getDefault(),
    private val baseDir: Path = fileSystem.getPath(".").toAbsolutePath().normalize()
) {

    fun expandGlobs(patterns: List<String>): List<Path> {
        return patterns.flatMap { pattern -> expandGlob(pattern) }.distinct()
    }

    fun expandGlob(pattern: String): List<Path> {
        val normalizedPattern = pattern.trim()

        if (!containsGlobPattern(normalizedPattern)) {
            val path = resolvePath(normalizedPattern)
            return if (Files.exists(path)) listOf(path) else emptyList()
        }

        return findMatchingFiles(normalizedPattern)
    }

    private fun containsGlobPattern(pattern: String): Boolean {
        return pattern.any { it in GLOB_CHARS }
    }

    private fun resolvePath(pathString: String): Path {
        val path = fileSystem.getPath(pathString)
        return if (path.isAbsolute) path else baseDir.resolve(path)
    }

    private fun findMatchingFiles(pattern: String): List<Path> {
        val globPattern = "glob:$pattern"

        val matcher: PathMatcher = try {
            fileSystem.getPathMatcher(globPattern)
        } catch (e: IllegalArgumentException) {
            return emptyList()
        }

        val matchingFiles = mutableListOf<Path>()

        val startPath = findStartPath(pattern)
        if (!Files.exists(startPath)) {
            return emptyList()
        }

        Files.walkFileTree(startPath, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val relativePath = baseDir.relativize(file)
                if (matcher.matches(relativePath)) {
                    matchingFiles.add(file)
                }
                return FileVisitResult.CONTINUE
            }

            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                val relativePath = baseDir.relativize(dir)
                if (matcher.matches(relativePath)) {
                    matchingFiles.add(dir)
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: java.io.IOException?): FileVisitResult {
                return FileVisitResult.CONTINUE
            }
        })

        return matchingFiles
    }

    private fun findStartPath(pattern: String): Path {
        val parts = pattern.split("/", "\\")
        val staticParts = parts.takeWhile { !containsGlobPattern(it) }

        return if (staticParts.isEmpty()) {
            baseDir
        } else {
            var path = baseDir
            for (part in staticParts) {
                path = path.resolve(part)
            }
            if (Files.exists(path)) path else baseDir
        }
    }

    private companion object {
        const val GLOB_CHARS = "*?[{"
    }
}
