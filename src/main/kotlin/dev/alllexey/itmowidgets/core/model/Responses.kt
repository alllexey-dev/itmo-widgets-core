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

data class SportFreeSignEntry(
    val id: Long,
    val lessonId: Long,
    val position: Int,
    val total: Int,
    val status: QueueEntryStatus,
    val createdAt: OffsetDateTime,
    val notifiedAt: OffsetDateTime?,
    val lessonData: BasicSportLessonData,
)

data class SportFreeSignQueue(
    val lessonId: Long,
    val total: Int,
)

data class SportAutoSignEntry(
    val id: Long,
    val prototypeLessonId: Long,
    val realLessonId: Long?,
    val position: Int,
    val total: Int,
    val status: QueueEntryStatus,
    val createdAt: OffsetDateTime,
    val notifiedAt: OffsetDateTime?,
    val prototypeLessonData: BasicSportLessonData,
    val realLessonData: BasicSportLessonData?,
)

data class SportAutoSignQueue(
    val prototypeLessonId: Long,
    val total: Int
)

data class SportAutoSignLimits(
    val limit: Int,
    val available: Int,
    val nextAvailableAt: OffsetDateTime,
)