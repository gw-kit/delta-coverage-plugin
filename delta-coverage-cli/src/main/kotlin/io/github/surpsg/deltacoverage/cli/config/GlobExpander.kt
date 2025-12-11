package io.github.surpsg.deltacoverage.cli.config

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

object GlobExpander {

    private const val GLOB_CHARS = "*?[{"

    fun expandGlobs(patterns: List<String>, baseDir: File = File(".")): List<File> {
        return patterns.flatMap { pattern -> expandGlob(pattern, baseDir) }.distinct()
    }

    fun expandGlob(pattern: String, baseDir: File = File(".")): List<File> {
        val normalizedPattern = pattern.trim()

        if (!containsGlobPattern(normalizedPattern)) {
            val file = resolveFile(normalizedPattern, baseDir)
            return if (file.exists()) listOf(file) else emptyList()
        }

        return findMatchingFiles(normalizedPattern, baseDir)
    }

    private fun containsGlobPattern(pattern: String): Boolean {
        return pattern.any { it in GLOB_CHARS }
    }

    private fun resolveFile(path: String, baseDir: File): File {
        val file = File(path)
        return if (file.isAbsolute) file else File(baseDir, path)
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException", "ReturnCount")
    private fun findMatchingFiles(pattern: String, baseDir: File): List<File> {
        val basePath = baseDir.toPath().toAbsolutePath().normalize()
        val globPattern = "glob:$pattern"

        val matcher: PathMatcher = try {
            FileSystems.getDefault().getPathMatcher(globPattern)
        } catch (e: IllegalArgumentException) {
            return emptyList()
        }

        val matchingFiles = mutableListOf<File>()

        val startPath = findStartPath(pattern, basePath)
        if (!Files.exists(startPath)) {
            return emptyList()
        }

        Files.walkFileTree(startPath, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val relativePath = basePath.relativize(file)
                if (matcher.matches(relativePath)) {
                    matchingFiles.add(file.toFile())
                }
                return FileVisitResult.CONTINUE
            }

            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                val relativePath = basePath.relativize(dir)
                if (matcher.matches(relativePath)) {
                    matchingFiles.add(dir.toFile())
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: java.io.IOException?): FileVisitResult {
                return FileVisitResult.CONTINUE
            }
        })

        return matchingFiles
    }

    private fun findStartPath(pattern: String, basePath: Path): Path {
        val parts = pattern.split("/", "\\")
        val staticParts = parts.takeWhile { !containsGlobPattern(it) }

        return if (staticParts.isEmpty()) {
            basePath
        } else {
            var path = basePath
            for (part in staticParts) {
                path = path.resolve(part)
            }
            if (Files.exists(path)) path else basePath
        }
    }
}
