package dev.alllexey.itmowidgets.core.model.social

import dev.alllexey.itmowidgets.core.model.UserData

/** Identity and viewer-scoped capabilities; owner privacy settings are never present. */
data class UserProfile(val user: UserData, val relationship: RelationshipState)

/**
 * Annotate MyITMO search results with registered Backend profiles; not a name search.
 * At most 50 positive ISUs, including duplicates. Empty lists are valid.
 */
data class UserLookupRequest(val isus: List<Int>)

/** Unknown ISUs are omitted, duplicates removed, first-occurrence request order preserved. */
data class UserLookupResponse(val users: List<UserProfile>)
