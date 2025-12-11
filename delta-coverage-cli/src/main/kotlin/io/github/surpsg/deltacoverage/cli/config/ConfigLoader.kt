package io.github.surpsg.deltacoverage.cli.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

object ConfigLoader {

    private val yamlMapper: ObjectMapper by lazy {
        ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule.Builder().build())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun loadFromFile(configFile: File): CliConfig {
        require(configFile.exists()) { "Configuration file not found: ${configFile.absolutePath}" }
        require(configFile.isFile) { "Configuration path is not a file: ${configFile.absolutePath}" }

        return yamlMapper.readValue(configFile)
    }
}
