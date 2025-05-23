package pl.digsa.flutter_arb_action.autohinting

import org.junit.Test
import pl.digsa.flutter_arb_action.utils.getRestOfHintToShow

class RestToHintTest {
    @Test
    fun `when entered fully previous part, should show entire hint`() {
        val enteredText = "home"
        val hint = "Title"

        assert(getRestOfHintToShow(enteredText, hint) == hint)
    }

    @Test
    fun `when entered empty current text, should show entire hint`() {
        val enteredText = ""
        val hint = "home"

        assert(getRestOfHintToShow(enteredText, hint) == hint)
    }

    @Test
    fun `when entered partially first part, should show rest of hint`() {
        val enteredText = "ho"
        val hint = "home"

        assert(getRestOfHintToShow(enteredText, hint) == "me")
    }

    @Test
    fun `when entered partially second part, should show rest of hint`() {
        val enteredText = "homeTi"
        val hint = "Title"

        assert(getRestOfHintToShow(enteredText, hint) == "tle")
    }
}