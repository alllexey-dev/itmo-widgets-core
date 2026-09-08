package dev.alllexey.itmowidgets.core.model

/**
 * The owner's audience for one data type. The viewer's own privacy settings do
 * not affect access.
 */
enum class SharingVisibility {
    /** Any authenticated application user. */
    ALL,

    /** Mutual friends only; the server default for a new account. */
    FRIENDS,

    /** The owner only. */
    NOBODY
}

/**
 * Own settings returned by `/api/users/me/privacy`; never exposed for another
 * user. Both fields are required on GET and PUT. Missing or unknown wire values
 * are errors, not an implicit request to broaden the audience to FRIENDS.
 */
data class UserPrivacySettings(
    val scheduleVisibility: SharingVisibility,
    val sportVisibility: SharingVisibility
)

/**
 * Confirmed sport lesson IDs visible to the authenticated caller for the
 * requested user. This response contains no pending queues or predictions.
 */
data class UserSportBookingsResponse(
    val lessonIds: List<Long>
)
