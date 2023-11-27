package io.github.surpsg.deltacoverage.gradle.git

import io.github.surpsg.deltacoverage.diff.git.getCrlf
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

fun buildGitRepository(rootProjectDir: File): Git {
    val gitDir = File(rootProjectDir, ".git")
    val repository: Repository = FileRepositoryBuilder.create(gitDir).apply {
        config.setEnum(
            ConfigConstants.CONFIG_CORE_SECTION,
            null,
            ConfigConstants.CONFIG_KEY_AUTOCRLF,
            getCrlf()
        )
        if (!gitDir.exists()) {
            create()
        }
    }
    return Git(repository)
}
