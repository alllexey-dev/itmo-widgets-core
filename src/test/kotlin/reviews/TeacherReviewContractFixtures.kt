package reviews

import dev.alllexey.itmowidgets.core.model.UserCapabilities
import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.resources.*
import dev.alllexey.itmowidgets.core.model.reviews.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

internal object TeacherReviewContractFixtures {
    const val TEACHER = 100001
    const val PROVIDER_URL = "https://onetwozzzplus.github.io/reviews/#/teacher/100001"
    const val TEXT = "Синтетический отзыв о преподавателе для проверки контракта"
    val namedId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000071")
    val anonymousId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000072")
    val copyId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000073")
    val ownId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000074")
    val revisionId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000075")
    val approvedId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000076")
    val caseId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000077")
    val now: OffsetDateTime = OffsetDateTime.parse("2026-09-29T09:00:00Z")
    val author = UserData(123456, "Synthetic author", null, emptyList(), UserCapabilities(false, false, false))

    val named = TeacherReview(namedId, TeacherReviewKind.COMMUNITY, "Математика", LocalDate.of(2026, 9, 23), null, TEXT,
        3, 1, true, false, author, null, null)
    val anonymous = named.copy(id = anonymousId, subjectTitle = null, score = 0, myVote = 0, verified = false,
        reportedByMe = true, author = null)
    val copy = TeacherReview(copyId, TeacherReviewKind.REVIEWS, "Физика", null, 2024, TEXT, -1, -1, false, false, null,
        "Тестовый источник", "https://example.test/review")
    val mine = OwnTeacherReview(ownId, "Математика", TEXT, true, TeacherReviewStatus.REJECTED, "Не о преподавателе", 0, false,
        LocalDate.of(2026, 9, 28))
    val response = TeacherReviewsResponse(TEACHER, PROVIDER_URL, listOf(named, anonymous, copy), mine,
        canWrite = true, canVote = true, canReport = true, knownTeacher = true)
    val save = SaveTeacherReviewRequest("Математика", TEXT, false, listOf(93724, 93725))

    val revision = TeacherReviewRevision(revisionId, ownId, 2, "Математика", TEXT, ReviewRevisionStatus.PENDING, now, null, null)
    val approved = revision.copy(id = approvedId, number = 1, status = ReviewRevisionStatus.APPROVED, decidedAt = now,
        note = "Проверено")
    val moderated = ModeratedTeacherReview(ownId, TEACHER, "Синтетический преподаватель", true, TeacherReviewStatus.PENDING,
        null, approved, 2, false, ReviewVerification.VERIFIED, 93724)
    val report = ModerationReport(ReportReason.OFFENSIVE, "Грубо", now)
    val history = SubmitterHistory(1, 0, 0, emptyList())
    val target = TeacherReviewTarget(revision, moderated, author, listOf(report), history)
    val case = ModerationCase(caseId, ModerationTargetType.TEACHER_REVIEW, ModerationCaseStatus.OPEN,
        ModerationCaseReason.SUBMISSION, now, target, emptyList())
}
