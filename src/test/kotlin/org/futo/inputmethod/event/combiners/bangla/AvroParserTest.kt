package org.futo.inputmethod.event.combiners.bangla

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Sanity checks for the Avro transliteration table/parser. Expected values are
 * what the reference implementation (OpenBangla / okkhor) produces.
 */
class AvroParserTest {
    private fun check(input: String, expected: String) {
        assertEquals("convert(\"$input\")", expected, AvroParser.convert(input))
    }

    @Test
    fun words() {
        check("amar", "আমার")
        check("ami", "আমি")
        check("bangla", "বাংলা")
        check("kemon", "কেমন")
        check("tumi", "তুমি")
        check("kotha", "কথা")
        check("korbo", "করব")
        check("na", "না")
        check("ki", "কি")
        check("bhalo", "ভাল")
        check("kkh", "ক্ষ")
        check("rri", "ঋ")
        check("sh", "শ")
        check("Sh", "ষ")
        check("t", "ত")
        check("T", "ট")
        check("th", "থ")
    }

    @Test
    fun independentVowels() {
        check("o", "অ")
        check("O", "ও")
        check("OI", "ঐ")
        check("OU", "ঔ")
        check("oo", "উ")
        check("aT", "আট")
    }

    @Test
    fun digitsAndPunctuation() {
        check("0", "০")
        check("123", "১২৩")
        check(".", "।")
        check("..", "।।")
    }

    @Test
    fun hasantaEscape() {
        // '`' produces the explicit hasanta form (no inherent vowel)
        check("o`", "")
        check("k`", "ক")
    }

    @Test
    fun capitalizedLetterKeepsItsAvroMeaning() {
        // 'T' is case sensitive (ট), 'A' is not (আ)
        check("T", "ট")
        check("A", "আ")
    }
}
