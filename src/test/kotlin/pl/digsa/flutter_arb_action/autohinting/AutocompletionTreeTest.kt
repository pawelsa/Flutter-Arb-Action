package pl.digsa.flutter_arb_action.autohinting

import org.junit.Test

class AutocompletionTreeTest {

    @Test
    fun `should concat name and build simple tree with single branches`() {
        val input = listOf("searchDialogTitle")
        val tree = AutocompletionTree(input)

        assert(tree.findMatching("sea") == "search")
        assert(tree.findMatching("search") == "searchDialog")
        assert(tree.findMatching("searchDia") == "searchDialog")
        assert(tree.findMatching("searchDialogT") == "searchDialogTitle")
    }

    @Test
    fun `should concat name and build a tree`() {
        val input = listOf("searchDialogTitle", "searchSheetTitle", "appbar", "appbarTitle", "search")
        val tree = AutocompletionTree(input)

        assert(tree.findMatching("sea") == "search")
        assert(tree.findMatching("search") == "searchDialog")
        assert(tree.findMatching("searchDia") == "searchDialog")
        assert(tree.findMatching("searchDialogT") == "searchDialogTitle")
        assert(tree.findMatching("ap") == "appbar")
        assert(tree.findMatching("appbar") == "appbarTitle")
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
        val tree = AutocompletionTree(input)

        assert(tree.findMatching("sea") == "search")
        assert(tree.findMatching("search") == "searchPersonnel")
        assert(tree.findMatching("searchP") == "searchPersonnel")
        assert(tree.findMatching("searchM") == "searchMedical")
    }

}