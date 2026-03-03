package io.github.gwkit.testimpact.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Task that generates a JFC (Java Flight Configuration) file
 * optimized for stack trace sampling.
 */
abstract class GenerateJfcConfigTask @Inject constructor() : DefaultTask() {

    init {
        group = "verification"
        description = "Generates JFC config file for JFR stack trace sampling"
    }

    @get:OutputFile
    abstract val jfcFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val file = jfcFile.get().asFile
        file.parentFile?.mkdirs()
        file.writeText(JFC_CONFIG)
        logger.debug("Created JFC config file: {}", file.absolutePath)
    }

    companion object {
        /**
         * JFC configuration optimized for stack trace sampling.
         * - ExecutionSample at 1ms interval for frequent sampling
         * - All other events disabled to minimize overhead
         */
        private val JFC_CONFIG = """
            <?xml version="1.0" encoding="UTF-8"?>
            <configuration version="2.0">
                <event name="jdk.ExecutionSample">
                    <setting name="enabled">true</setting>
                    <setting name="period">1 ms</setting>
                    <setting name="stackTrace">true</setting>
                </event>
                <event name="jdk.NativeMethodSample">
                    <setting name="enabled">false</setting>
                </event>
                <event name="jdk.ActiveRecording">
                    <setting name="enabled">false</setting>
                </event>
                <event name="jdk.ActiveSetting">
                    <setting name="enabled">false</setting>
                </event>
            </configuration>
        """.trimIndent()
    }
}
