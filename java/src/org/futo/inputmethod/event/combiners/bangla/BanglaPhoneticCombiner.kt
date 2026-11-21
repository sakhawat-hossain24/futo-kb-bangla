package org.futo.inputmethod.event.combiners.bangla

import org.futo.inputmethod.event.Combiner
import org.futo.inputmethod.event.Event
import org.futo.inputmethod.latin.common.Constants
import java.util.ArrayList

/**
 * Combiner that transliterates Avro Phonetic latin input into Bangla.
 *
 * The user types on a latin keyboard ("amar"), the combiner keeps the typed
 * characters in a buffer and reports the Bangla equivalent ("আমার") as the
 * combining state, so it is displayed as composing text while typing. Space
 * and other functional keys end the word and commit the Bangla text.
 *
 * The raw latin buffer is published through [BanglaPhoneticState] so the
 * suggestion strip can show the typed latin text next to the Bangla
 * suggestions.
 */
class BanglaPhoneticCombiner : Combiner {
    // Holds a single word as typed in latin characters.
    private val buffer = StringBuilder()

    private fun isAvroChar(c: Char): Boolean {
        // Letters, digits, and the punctuation that the Avro table converts
        // (e.g. '.' -> '।', ".." -> "।।"); '`' is the scheme's escape character.
        return (c in 'a'..'z') || (c in 'A'..'Z') || (c in '0'..'9') ||
                c == '`' || c == '.' || c == ',' || c == '?' || c == '!' ||
                c == ':' || c == ';' || c == '\'' || c == '"' ||
                c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' ||
                c == '-' || c == '_' || c == '/' || c == '\\' || c == '@' || c == '#' ||
                c == '%' || c == '&' || c == '*' || c == '+' || c == '=' || c == '<' ||
                c == '>' || c == '|' || c == '~' || c == '^' || c == '$'
    }

    override fun processEvent(previousEvents: ArrayList<Event?>?, event: Event?): Event {
        // Never return Event.createNotHandledEvent() here: the input pipeline still walks the
        // returned event, and a NOT_HANDLED one is "functional" with key code 0, which lands in
        // InputLogic's "Unknown key code : 0" debug crash. Declining an event means returning it
        // unchanged.
        if (event == null) return Event.createNotHandledEvent()
        if (event.eventType != Event.EVENT_TYPE_INPUT_KEYPRESS) return event

        if (event.mKeyCode == Constants.CODE_DELETE) {
            if (buffer.isNotEmpty()) {
                buffer.setLength(buffer.length - 1)
                publish()
                return Event.createConsumedEvent(event)
            }
            return event
        }

        if (event.isFunctionalKeyEvent) return event

        val keypress = event.mCodePoint.toChar()
        if (!isAvroChar(keypress)) return event

        buffer.append(keypress)
        publish()
        return Event.createConsumedEvent(event)
    }

    override fun getCombiningStateFeedback(): CharSequence {
        val converted = AvroParser.convert(buffer.toString())
        publish()
        return converted
    }

    private fun publish() = BanglaPhoneticState.update(buffer.toString())

    override fun reset() {
        buffer.setLength(0)
        publish()
    }
}
