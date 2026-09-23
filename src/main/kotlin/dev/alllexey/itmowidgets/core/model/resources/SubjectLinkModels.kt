package dev.alllexey.itmowidgets.core.model.resources

import dev.alllexey.itmowidgets.core.model.UserData
import java.time.OffsetDateTime
import java.util.UUID

/** Approved MATERIALS, TASKS, RECORDINGS, NOTES and EXAM links are also shown to later periods. */
enum class LinkCategory { SCORES, QUEUE, MATERIALS, TASKS, RECORDINGS, NOTES, EXAM, CHAT, OTHER }

/** GROUP and FLOW publish to the author's schedule flows at once; ALL may be premoderated. */
enum class LinkVisibility { PRIVATE, GROUP, FLOW, ALL }

/** Owners see their own link's review state; other viewers always receive PUBLISHED. */
enum class SubjectLinkStatus { PRIVATE, PENDING, PUBLISHED, REJECTED, HIDDEN }

enum class LinkRevisionStatus { PENDING, APPROVED, REJECTED, WITHDRAWN }

/** A period-scoped link as seen by the authenticated viewer; others see the last approved content. */
data class SubjectLink(
    val id: UUID,
    val subjectId: Long,
    val subjectName: String,
    val periodKey: String,
    val category: LinkCategory,
    val url: String,
    val title: String?,
    val visibility: LinkVisibility,
    val audienceLabel: String?,
    val status: SubjectLinkStatus,
    val reviewNote: String?,
    val score: Int,
    val myVote: Int,
    val isMine: Boolean,
    val isSaved: Boolean,
    val reportedByMe: Boolean,
    val author: UserData?,
    val updatedAt: OffsetDateTime,
)

/** A GROUP or FLOW audience the viewer can publish to, labelled with its schedule group names. */
data class LinkAudience(val visibility: LinkVisibility, val label: String)

/** mine holds the viewer's links, shared the visible links of others, previous approved links of past periods. */
data class SubjectLinksResponse(
    val mine: List<SubjectLink>,
    val shared: List<SubjectLink>,
    val previous: List<SubjectLink>,
    val pinnedId: UUID?,
    val audiences: List<LinkAudience>,
    val premoderation: Boolean,
)

/** An immutable sent snapshot of a non-private link; only the outcome fields change. */
data class SubjectLinkRevision(
    val id: UUID,
    val linkId: UUID,
    val number: Int,
    val category: LinkCategory,
    val url: String,
    val title: String?,
    val visibility: LinkVisibility,
    val status: LinkRevisionStatus,
    val submittedAt: OffsetDateTime,
    val decidedAt: OffsetDateTime?,
    val note: String?,
)

/** Creates a link under a client-generated ID or replaces the viewer's own link. */
data class SaveSubjectLinkRequest(
    val subjectId: Long,
    val subjectName: String,
    val periodKey: String,
    val category: LinkCategory,
    val url: String,
    val title: String?,
    val visibility: LinkVisibility,
)

data class SetLinkSavedRequest(val saved: Boolean)

/** A null linkId removes the pin for the period. */
data class PinSubjectLinkRequest(val periodKey: String, val linkId: UUID? = null)

/** -1 or +1 replaces a previous vote; 0 removes it. Own links cannot be voted on. */
data class ResourceVoteRequest(val value: Int)
