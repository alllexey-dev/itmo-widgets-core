package dev.alllexey.itmowidgets.core.model

/**
 * The owner's audience for one data type. The viewer's own privacy settings do
 * not affect access.
 */
enum class SharingVisibility {
    /** Any authenticated application user. */
    ALL,

    /** Mutual friends only; the server default for schedule and sport. */
    FRIENDS,

    /** The owner only. */
    NOBODY
}

/**
 * Own settings returned by `/api/users/me/privacy`; never exposed for another
 * user. All three fields are required on GET and PUT. Missing or unknown wire values
 * are errors, not an implicit request to broaden the audience to FRIENDS.
 */
data class UserPrivacySettings @JvmOverloads constructor(
    val scheduleVisibility: SharingVisibility,
    val sportVisibility: SharingVisibility,
    val friendsVisibility: SharingVisibility = SharingVisibility.ALL
)

/**
 * Sport activity authorized by the owner's canViewSport capability.
 * Confirmed current/upcoming IDs are separate from active free/auto-sign queues.
 * Entries use the same polymorphic shape as FriendSportBooking.entry.
 * Older servers that omit entries decode to an empty list.
 */
data class UserSportBookingsResponse @JvmOverloads constructor(
    val lessonIds: List<Long>,
    val entries: List<SportQueueEntry> = emptyList(),
)
