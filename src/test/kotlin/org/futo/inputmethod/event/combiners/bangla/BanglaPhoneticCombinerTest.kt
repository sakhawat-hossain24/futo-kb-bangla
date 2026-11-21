package org.futo.inputmethod.event.combiners.bangla

import org.futo.inputmethod.event.Event
import org.futo.inputmethod.latin.common.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayList

/**
 * Behaviour of the combiner in the IME event pipeline.
 *
 * Regression guard: an event that the combiner does not want must be returned unchanged. Returning
 * Event.createNotHandledEvent() instead makes InputLogic treat it as a functional event with key
 * code 0 and throw "Unknown key code : 0" (observed when pressing backspace with an empty buffer).
 */
class BanglaPhoneticCombinerTest {
    private val combiner = BanglaPhoneticCombiner()
    private val previous = ArrayList<Event?>()

    private fun keyPress(codePoint: Int) =
        Event.createSoftwareKeypressEvent(codePoint, Event.NOT_A_KEY_CODE, 0, 0, false)

    private fun delete() =
        Event.createSoftwareKeypressEvent(Event.NOT_A_CODE_POINT, Constants.CODE_DELETE, 0, 0, false)

    private fun type(text: String) {
        for (c in text) {
            val processed = combiner.processEvent(previous, keyPress(c.code))
            assertTrue("'$c' should be consumed", processed.isConsumed)
        }
    }

    @Test
    fun lettersBecomeComposingText() {
        type("amar")
        assertEquals("আমার", combiner.getCombiningStateFeedback().toString())
    }

    @Test
    fun backspaceTrimsTheBuffer() {
        type("amar")
        val processed = combiner.processEvent(previous, delete())
        assertTrue(processed.isConsumed)
        assertEquals("আমা", combiner.getCombiningStateFeedback().toString())
    }

    @Test
    fun backspaceOnEmptyBufferIsPassedThroughUnchanged() {
        val event = delete()
        val processed = combiner.processEvent(previous, event)
        assertSame("the event itself must be returned", event, processed)
        assertFalse(processed.isConsumed)
        assertEquals(
            "must not be turned into a NOT_HANDLED event",
            Event.EVENT_TYPE_INPUT_KEYPRESS, processed.eventType
        )
        assertEquals(Constants.CODE_DELETE, processed.mKeyCode)
    }

    @Test
    fun nonAvroCharactersArePassedThroughUnchanged() {
        val space = keyPress(' '.code)
        assertSame(space, combiner.processEvent(previous, space))
    }

    @Test
    fun resetClearsTheBuffer() {
        type("amar")
        combiner.reset()
        assertEquals("", combiner.getCombiningStateFeedback().toString())
    }

    @Test
    fun afterCommitTheBufferStartsEmpty() {
        type("kotha")
        combiner.reset()
        type("bhalo")
        assertEquals("ভাল", combiner.getCombiningStateFeedback().toString())
    }
}
