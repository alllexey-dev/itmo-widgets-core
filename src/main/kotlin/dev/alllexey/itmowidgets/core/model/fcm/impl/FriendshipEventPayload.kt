package dev.alllexey.itmowidgets.core.model.fcm.impl

import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.fcm.FcmPayload
import java.time.OffsetDateTime

enum class FriendshipEvent { REQUEST_RECEIVED, REQUEST_ACCEPTED }

/**
 * After-commit friendship event. [user] is the actor, with capabilities computed for
 * the recipient; [occurredAt] is the transition time, not the delivery time.
 * Reject, cancel, remove and idempotent retries do not produce an event.
 */
data class FriendshipEventPayload(
    val event: FriendshipEvent,
    val user: UserData,
    val occurredAt: OffsetDateTime,
) : FcmPayload {
    override fun getType(): String = TYPE

    companion object { const val TYPE = "FRIENDSHIP_EVENT_PAYLOAD" }
}
