package org.futo.inputmethod.event.combiners.bangla

/**
 * Avro Phonetic transliteration engine.
 *
 * Kotlin port of the phonetic parser in the `okkhor` crate 0.10.0
 * (https://crates.io/crates/okkhor, MIT License, (c) 2021 Gulshanur Rahman),
 * which implements the Avro Phonetic scheme by Mehdi Hasan Khan (OmicronLab).
 *
 * The algorithm is a greedy longest-match replacement over the latin input:
 * at every position the longest known pattern is looked up in the table, the
 * adjacent characters decide which conditional replacement applies, and the
 * matched text is replaced. Matching is done against the raw latin input, so
 * the rules see the same characters the user typed.
 */
internal object AvroParser {

    enum class Type { VOWEL, CONSONANT, PUNCTUATION, NUMBER, EXACT }

    /** A single condition on the character before or after the match. */
    class Cond(
        val prefix: Boolean,
        val negate: Boolean,
        val type: Type,
        val exact: Char = '\u0000',
    ) {
        fun satisfies(prefixChar: Char, suffixChar: Char): Boolean {
            val c = if (prefix) prefixChar else suffixChar
            val result = when (type) {
                Type.VOWEL -> isVowel(c)
                Type.CONSONANT -> isConsonant(c)
                Type.PUNCTUATION -> isPunctuation(c)
                Type.NUMBER -> c in '0'..'9'
                Type.EXACT -> c == exact
            }
            return if (negate) !result else result
        }
    }

    class Rule(val whenMatches: List<Cond>, val replaceWith: String)

    class Pattern(
        val find: String,
        val defaultReplacement: String,
        val rules: List<Rule> = emptyList(),
    ) {
        fun replacementFor(prefix: Char, suffix: Char): String {
            for (rule in rules) {
                var all = true
                for (cond in rule.whenMatches) {
                    if (!cond.satisfies(prefix, suffix)) {
                        all = false
                        break
                    }
                }
                if (all) return rule.replaceWith
            }
            return defaultReplacement
        }
    }

    private val byFind: Map<String, Pattern> = AvroPatterns.patterns.associateBy { it.find }
    private val maxFindLength: Int = AvroPatterns.patterns.maxOf { it.find.length }

    // Composing feedback is requested more than once per keypress; avoid redoing
    // the conversion for the same buffer.
    private var cachedInput: String? = null
    private var cachedOutput: String = ""

    fun convert(rawInput: String): String {
        if (rawInput == cachedInput) return cachedOutput
        val result = convertUncached(rawInput)
        cachedInput = rawInput
        cachedOutput = result
        return result
    }

    private fun convertUncached(rawInput: String): String {
        if (rawInput.isEmpty()) return ""

        val input = StringBuilder(rawInput.length)
        for (c in rawInput) input.append(conditionalLowercase(c))

        val output = StringBuilder(64)
        var prefix = ' '
        var i = 0
        val length = input.length

        while (i < length) {
            val pattern = findPattern(input, i)
            if (pattern != null) {
                val end = i + pattern.find.length
                val suffix = if (end < length) input[end] else ' '
                output.append(pattern.replacementFor(prefix, suffix))
                prefix = pattern.find[pattern.find.length - 1]
                i = end
            } else {
                val c = input[i]
                prefix = c
                output.append(c)
                i++
            }
        }

        return output.toString()
    }

    /** Longest pattern that matches at [offset], as the reference parser does. */
    private fun findPattern(input: StringBuilder, offset: Int): Pattern? {
        val remaining = input.length - offset
        var len = if (remaining < maxFindLength) remaining else maxFindLength
        while (len > 0) {
            val candidate = input.substring(offset, offset + len)
            val pattern = byFind[candidate]
            if (pattern != null) return pattern
            len--
        }
        return null
    }

    /**
     * The Avro scheme is case-sensitive only for these letters; everything else
     * is folded to lowercase before matching.
     */
    private fun conditionalLowercase(c: Char): Char {
        if (c in 'A'..'Z' && c.lowercaseChar() in "oiudgjnrstyz") return c
        return c.lowercaseChar()
    }

    private fun isVowel(c: Char): Boolean =
        c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' ||
                c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U'

    private fun isConsonant(c: Char): Boolean =
        !isVowel(c) && ((c in 'a'..'z') || (c in 'A'..'Z'))

    private fun isPunctuation(c: Char): Boolean = !((c in 'a'..'z') || (c in 'A'..'Z'))
}
