package resources

import dev.alllexey.itmowidgets.core.model.UserCapabilities
import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.resources.*
import java.time.OffsetDateTime
import java.util.UUID

internal object ResourceContractFixtures {
    val id: UUID = UUID.fromString("00000000-0000-0000-0000-000000000042")
    val otherId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000043")
    val now: OffsetDateTime = OffsetDateTime.parse("2026-09-22T09:00:00Z")
    val user = UserData(123456, "Synthetic user", null, emptyList(), UserCapabilities(false, false, false))
    val link = SubjectLink(id, 42, "Предмет", "2026-1", LinkCategory.SCORES,
        "https://docs.google.com/spreadsheets/d/example#gid=1", "Баллы", LinkVisibility.GROUP, "P3119, P3120",
        SubjectLinkStatus.PUBLISHED, null, 2, 1, false, true, false, user, now)
    val ownLink = link.copy(id = otherId, category = LinkCategory.CHAT, url = "https://t.me/example", title = null,
        visibility = LinkVisibility.ALL, audienceLabel = null, status = SubjectLinkStatus.REJECTED, reviewNote = "Не по предмету",
        score = 0, myVote = 0, isMine = true, isSaved = false, author = null)
    val previousLink = link.copy(periodKey = "2025-1", category = LinkCategory.MATERIALS, visibility = LinkVisibility.ALL,
        audienceLabel = null, myVote = -1, isSaved = false, reportedByMe = true)
    val audience = LinkAudience(LinkVisibility.FLOW, "P3119, P3120")
    val links = SubjectLinksResponse(listOf(ownLink), listOf(link), listOf(previousLink), id,
        listOf(LinkAudience(LinkVisibility.GROUP, "P3119"), audience), true)
    val revision = SubjectLinkRevision(otherId, id, 2, LinkCategory.MATERIALS, previousLink.url, "Материалы",
        LinkVisibility.ALL, LinkRevisionStatus.APPROVED, now, now, "Проверено")
    val pendingRevision = revision.copy(status = LinkRevisionStatus.PENDING, decidedAt = null, note = null, title = null)
    val save = SaveSubjectLinkRequest(42, "Предмет", "2026-1", LinkCategory.QUEUE, "https://example.org/queue", null,
        LinkVisibility.FLOW)
    val restriction = UserRestriction(id, RestrictionCapability.VOTE, "Правила", now, null)
    val report = ModerationReport(ReportReason.BROKEN, "Не открывается", now)
    val decision = ModerationDecision(id, id, ModerationAction.HIDE, "Проверено", null, now)
    val history = SubmitterHistory(1, 0, 2, listOf(restriction))
    val target = SubjectLinkTarget(revision, previousLink, user, listOf(report), history)
    val case = ModerationCase(id, ModerationTargetType.SUBJECT_RESOURCE, ModerationCaseStatus.OPEN,
        ModerationCaseReason.REPORTS, now, target, listOf(decision))
    val settings = ModerationSettings(mapOf(ModerationTargetType.SUBJECT_RESOURCE to ModerationPolicy()))
}
