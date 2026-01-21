package io.github.surpsg.deltacoverage.cli.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.InputStream

object ConfigLoader {

    fun loadFromFile(configFile: File): CliConfig {
        require(configFile.exists()) { "Configuration file not found: ${configFile.absolutePath}" }
        require(configFile.isFile) { "Configuration path is not a file: ${configFile.absolutePath}" }

        val format = ConfigFormat.fromExtension(configFile.extension)
        return configFile.inputStream().use { format.mapper.readValue(it) }
    }

    fun loadFromStream(inputStream: InputStream, format: ConfigFormat = ConfigFormat.YAML): CliConfig {
        return inputStream.use { format.mapper.readValue(it) }
    }

    enum class ConfigFormat(
        private val extensions: Set<String>,
        objectMapperProvider: () -> ObjectMapper,
    ) {
        JSON(
            setOf("json"),
            { ObjectMapper() }
        ),

        YAML(
            setOf("yaml", "yml"),
            { ObjectMapper(YAMLFactory()) }
        );

        internal val mapper: ObjectMapper by lazy {
            objectMapperProvider()
                .registerModule(KotlinModule.Builder().build())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        companion object {
            fun fromExtension(extension: String): ConfigFormat {
                val normalizedExt = extension.lowercase()
                return entries.find { normalizedExt in it.extensions }
                    ?: error("Unsupported config file format: '$extension'. Supported formats: json, yaml, yml")
            }
        }
    }
}
