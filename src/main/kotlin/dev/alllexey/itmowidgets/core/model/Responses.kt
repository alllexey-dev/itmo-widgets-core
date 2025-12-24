package dev.alllexey.itmowidgets.core.model

import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

data class TokenResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long
)

data class SessionResponse(val tokenId: UUID, val lastUsed: Instant)

data class SportFilterResponse(
    val id: Long,
    val sectionIds: List<Long>,
    val buildingIds: List<Long>,
    val timeSlotIds: List<Long>,
    val teacherIds: List<Long>,
)

enum class QueueEntryStatus {
    WAITING,
    NOTIFIED,
    SATISFIED,
    EXPIRED
}

data class BasicSportLessonData(
    val id: Long,
    val sectionId: Long,
    val sectionName: String,
    val sectionLevel: Long,
    val buildingId: Long,
    val roomName: String,
    val dateStart: OffsetDateTime,
    val dateEnd: OffsetDateTime,
    val timeSlotId: Long,
    val teacherIsu: Long,
    val teacherFio: String,
)

sealed interface QueueEntry {
    val id: Long
    val position: Int
    val total: Int
    val status: QueueEntryStatus
    val createdAt: OffsetDateTime
    val notifiedAt: OffsetDateTime?

    /**
     * For FreeSign, this is the [SportFreeSignEntry.lessonData].
     * For AutoSign, this is the [SportAutoSignEntry.prototypeLessonData].
     */
    val targetLesson: BasicSportLessonData
}

data class SportFreeSignEntry(
    override val id: Long,
    val lessonId: Long,
    override val position: Int,
    override val total: Int,
    override val status: QueueEntryStatus,
    override val createdAt: OffsetDateTime,
    override val notifiedAt: OffsetDateTime?,
    val lessonData: BasicSportLessonData,
    val forceSign: Boolean,
) : QueueEntry {
    override val targetLesson: BasicSportLessonData get() = lessonData
}

data class SportFreeSignQueue(
    val lessonId: Long,
    val total: Int,
)

data class SportAutoSignEntry(
    override val id: Long,
    val prototypeLessonId: Long,
    val realLessonId: Long?,
    override val position: Int,
    override val total: Int,
    override val status: QueueEntryStatus,
    override val createdAt: OffsetDateTime,
    override val notifiedAt: OffsetDateTime?,
    val prototypeLessonData: BasicSportLessonData,
    val realLessonData: BasicSportLessonData?,
) : QueueEntry {
    override val targetLesson: BasicSportLessonData get() = prototypeLessonData
}

data class SportAutoSignQueue(
    val prototypeLessonId: Long,
    val total: Int
)

data class SportAutoSignLimits(
    val limit: Int,
    val available: Int,
    val nextAvailableAt: OffsetDateTime,
)