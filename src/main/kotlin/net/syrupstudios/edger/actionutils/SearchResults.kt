package net.syrupstudios.edger.actionutils

import net.syrupstudios.edger.actions.ActionType

data class SearchResults(
    val actions: List<ActionType>,
    val searchParams: ActionSearchParams,
    val page: Int,
    val pages: Int
)
