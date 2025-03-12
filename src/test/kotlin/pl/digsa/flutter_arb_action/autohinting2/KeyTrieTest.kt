package pl.digsa.flutter_arb_action.autohinting2

import org.junit.Test

class KeyTrieTest {

    @Test
    fun `should return all first parts of keys for empty input`() {
        val input = listOf(
            "searchDialogTitle",
            "searchSheetTitle",
            "appbar",
            "appbarTitle",
            "search"
        )
        val tree = KeyTrie()
        input.forEach(tree::insert)

        assert(tree.getNextSuggestions("") == listOf("search", "appbar"))
    }

    @Test
    fun `should concat name and build simple tree with single branches`() {
        val input = listOf("searchDialogTitle")
        val tree = KeyTrie()
        input.forEach(tree::insert)

        assert(tree.getNextSuggestions("sea") == listOf("search"))
        assert(tree.getNextSuggestions("search") == listOf("Dialog"))
        assert(tree.getNextSuggestions("searchDia") == listOf("Dialog"))
        assert(tree.getNextSuggestions("searchDialogT") == listOf("Title"))
    }

    @Test
    fun `should concat name and build a tree`() {
        val input = listOf(
            "searchDialogTitle",
            "searchSheetTitle",
            "appbar",
            "appbarTitle",
            "search"
        )
        val tree = KeyTrie()
        input.forEach(tree::insert)

        assert(tree.getNextSuggestions("sea") == listOf("search"))
        assert(tree.getNextSuggestions("search") == listOf("Dialog", "Sheet"))
        assert(tree.getNextSuggestions("searchDia") == listOf("Dialog"))
        assert(tree.getNextSuggestions("searchDialogT") == listOf("Title"))
        assert(tree.getNextSuggestions("ap") == listOf("appbar"))
        assert(tree.getNextSuggestions("appbar") == listOf("Title"))
    }

    @Test
    fun `should concat name and build a tree starting with the same word`() {
        val input = listOf(
            "searchPersonnelPesel",
            "searchPersonnelProfessionTitle",
            "searchMedicalServiceAppbarTitle",
            "searchMedicalServiceCriteriaTitle",
            "search"
        )
        val tree = KeyTrie()
        input.forEach(tree::insert)

//        assert(tree.getNextSuggestions("sea") == listOf("search"))
        assert(tree.getNextSuggestions("search") == listOf("Personnel", "Medical"))
        assert(tree.getNextSuggestions("searchP") == listOf("Personnel"))
        assert(tree.getNextSuggestions("searchPersonnel").also { println(it) } == listOf("Pesel", "Profession"))
        assert(tree.getNextSuggestions("searchM") == listOf("Medical"))
        assert(tree.getNextSuggestions("searchMedical") == listOf("Service"))
        assert(tree.getNextSuggestions("searchMedicalService") == listOf("Appbar", "Criteria"))
    }
}