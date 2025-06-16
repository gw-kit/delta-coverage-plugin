package io.github.surpsg.deltacoverage.gradle.config

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AntPatternMapperTest {

    @ParameterizedTest
    @CsvSource(
        value = [
            "src/test/resources, ^src/test/resources$",
            "src/main/java/**/*.java, ^src/main/java/.*/[^/]*\\.java$",
            "src/test/resources/*, ^src/test/resources/[^/]*$",
            "com/example/**/Test*.kt, ^com/example/.*/Test[^/]*\\.kt$",
            "src/**/file.txt, ^src/.*/file\\.txt$",
            "**/*.xml, ^.*/[^/]*\\.xml$",
            "docs/*/index.html, ^docs/[^/]*/index\\.html$",
            "index?.html, ^index[^/]\\.html$",
        ]
    )
    fun `should convert ant pattern to regex string`(
        antPattern: String,
        expectedRegex: String,
    ) {
        // WHEN
        val actualRegex = antPattern.antToRegex()

        // THEN
        actualRegex shouldBeEqual expectedRegex
    }
}
