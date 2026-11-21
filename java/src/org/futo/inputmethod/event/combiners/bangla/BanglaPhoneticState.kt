package org.futo.inputmethod.event.combiners.bangla

/**
 * The literal latin text of the Bangla phonetic word currently being composed.
 *
 * The combiner keeps the typed characters in a buffer and shows the Bangla conversion as
 * composing text, so the typed latin is otherwise invisible. The suggestion strip reads this to
 * offer a cell with the raw input, which commits the latin text instead of the conversion.
 */
object BanglaPhoneticState {
    /** Raw latin characters of the word being composed; empty when no word is being composed. */
    @Volatile
    var rawInput: String = ""
        private set

    fun update(raw: String) {
        if (raw != rawInput) rawInput = raw
    }

    fun clear() = update("")
}
