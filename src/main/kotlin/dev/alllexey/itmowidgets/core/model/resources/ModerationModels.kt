package dev.alllexey.itmowidgets.core.model.resources

import dev.alllexey.itmowidgets.core.model.UserData
import java.time.OffsetDateTime
import java.util.UUID

/** TEACHER_REVIEW is reserved for the future review feature, not produced by this Backend version. */
enum class ModerationTargetType { SUBJECT_RESOURCE, TEACHER_REVIEW }
enum class ModerationCaseStatus { OPEN, RESOLVED, WITHDRAWN }
enum class ModerationCaseReason { SUBMISSION, REPORTS, VOTES }
enum class ModerationActor { MODERATOR, POLICY }
enum class ModerationAction { APPROVE, REJECT, HIDE, RESTORE, DISMISS, RESTRICT_USER, HIDE_ALL_BY_USER }
enum class ReportReason { BROKEN, WRONG_SUBJECT, SPAM, OTHER }

/** Unknown future capabilities decode conservatively as ALL; missing/null values remain invalid. */
enum class RestrictionCapability { SUBMIT_RESOURCES, VOTE, REPORT, WRITE_REVIEWS, ALL }

/** Active restrictions block mutations, never read access. Null expiry means permanent. */
data class UserRestriction(val id: UUID, val capability: RestrictionCapability, val reason: String,
    val startsAt: OffsetDateTime, val expiresAt: OffsetDateTime?)

/** A null target means the reviewed link was deleted; the case and its decisions remain for audit. */
data class ModerationCase(val id: UUID, val targetType: ModerationTargetType, val status: ModerationCaseStatus,
    val reason: ModerationCaseReason, val openedAt: OffsetDateTime, val target: ModerationCaseTarget?,
    val decisions: List<ModerationDecision>)

/** The wire discriminator is targetType; unknown or unimplemented subtypes fail decoding. */
sealed interface ModerationCaseTarget

/** Wire targetType SUBJECT_RESOURCE: the reviewed revision, the link as the moderator sees it and its author. */
data class SubjectLinkTarget(val revision: SubjectLinkRevision, val link: SubjectLink, val author: UserData,
    val reports: List<ModerationReport>, val submitterHistory: SubmitterHistory) : ModerationCaseTarget

data class SubmitterHistory(val approved: Long, val rejected: Long, val dismissedReports: Long,
    val activeRestrictions: List<UserRestriction>)

/** Optional positive days; null means permanent. Only RESTRICT_USER accepts this payload. */
data class RestrictionRequest(val capability: RestrictionCapability, val days: Int? = null)

data class ModerationDecisionRequest(val action: ModerationAction, val note: String? = null, val restriction: RestrictionRequest? = null)

data class ModerationDecision(val id: UUID, val moderatorId: UUID?, val action: ModerationAction,
    val note: String?, val restriction: RestrictionRequest?, val createdAt: OffsetDateTime, val actor: ModerationActor = ModerationActor.MODERATOR)

/** Queue report summaries intentionally omit reporter identity. */
data class ModerationReport(val reason: ReportReason, val comment: String?, val createdAt: OffsetDateTime)
data class ModerationReportRequest(val reason: ReportReason, val comment: String? = null)

/** Constructor defaults are convenient locally; all five fields remain mandatory on the wire. */
data class ModerationPolicy(val premoderation: Boolean = true, val reportThreshold: Int = 3,
    val voteThreshold: Int = -3, val dailySubmissionLimit: Int = 5, val dailyReportLimit: Int = 10)

/** PUT is a complete replacement of the policies for every target supported by Backend. */
data class ModerationSettings(val policies: Map<ModerationTargetType, ModerationPolicy>)
