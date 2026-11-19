package org.futo.inputmethod.v2keyboard

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the layout YAML files against schema drift: every layout shipped in
 * assets must deserialize into [Keyboard]. Runs against the repo checkout, so it
 * needs no Android resources.
 */
class LayoutYamlTest {
    private val layoutsDir = File("java/assets/layouts")

    @Test
    fun bengaliLayoutsParse() {
        val files = File(layoutsDir, "Bengali").listFiles { f -> f.extension == "yaml" }
        assertTrue("no Bengali layouts found at ${layoutsDir.absolutePath}", !files.isNullOrEmpty())

        for (file in files!!) {
            val keyboard = parseKeyboardYamlString(file.readText())
            assertTrue("${file.name} has no name", keyboard.name.isNotBlank())
        }
    }

    @Test
    fun avroPhoneticLayoutUsesBanglaPhoneticCombiner() {
        val keyboard = parseKeyboardYamlString(
            File(layoutsDir, "Bengali/bengali_avro_phonetic.yaml").readText()
        )
        assertTrue(
            "expected BanglaPhonetic combiner, got ${keyboard.combiners}",
            keyboard.combiners.contains(CombinerKind.BanglaPhonetic)
        )
        assertTrue(
            "expected the layout to be offered for Bangla, got ${keyboard.languages}",
            keyboard.languages.contains("bn")
        )
    }
}
