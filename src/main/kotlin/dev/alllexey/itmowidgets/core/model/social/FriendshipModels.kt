package dev.alllexey.itmowidgets.core.model.social

/** Relationship to the authenticated viewer, not the direction stored in the database. */
enum class RelationshipState {
    /** No relationship; also used for a self profile, which cannot receive a request. */
    NONE,
    /** The viewer sent a pending request to this person. */
    OUTGOING,
    /** This person sent a pending request to the viewer. */
    INCOMING,
    /** An accepted, mutual friendship. */
    FRIENDS,
    /** Reserved for future server-side blocking; rejection/cancellation never means BLOCKED. */
    BLOCKED,
}
