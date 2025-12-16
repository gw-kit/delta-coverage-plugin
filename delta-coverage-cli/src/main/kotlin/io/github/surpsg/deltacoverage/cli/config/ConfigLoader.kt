package io.github.surpsg.deltacoverage.cli.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

object ConfigLoader {

    private val JSON_EXTENSIONS = setOf("json")
    private val YAML_EXTENSIONS = setOf("yaml", "yml")

    private val jsonMapper: ObjectMapper by lazy {
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val yamlMapper: ObjectMapper by lazy {
        ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule.Builder().build())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun loadFromFile(configFile: File): CliConfig {
        require(configFile.exists()) { "Configuration file not found: ${configFile.absolutePath}" }
        require(configFile.isFile) { "Configuration path is not a file: ${configFile.absolutePath}" }

        return resolveMapper(configFile).readValue(configFile)
    }

    private fun resolveMapper(configFile: File): ObjectMapper =
        when (val extension = configFile.extension.lowercase()) {
            in JSON_EXTENSIONS -> jsonMapper
            in YAML_EXTENSIONS -> yamlMapper
            else -> error(
                "Unsupported config file format: '$extension'. Supported formats: json, yaml, yml"
            )
        }
}
