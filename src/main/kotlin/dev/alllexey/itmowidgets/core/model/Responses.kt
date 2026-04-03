package dev.alllexey.itmowidgets.core.model

import java.time.OffsetDateTime

// region user

data class UserSettings(
    val sportSharing: Boolean,
    val scheduleSharing: Boolean,
)

data class UserData(
    val isu: Int,
    val name: String,
    val pictureUrl: String?,
    val groups: List<GroupData>,
    val settings: UserSettings
)

data class GroupData(
    val name: String,
    val course: Int,
    val facultyShortName: String,
)

// endregion user

// region sport

enum class QueueEntryStatus {
    WAITING,
    NOTIFIED,
    GAVE_UP_NOTIFYING,
    SATISFIED,
    EXPIRED;

    val isNotifiable get() = this == WAITING || this == GAVE_UP_NOTIFYING

    companion object {
        val notifiableStatuses = listOf(WAITING, NOTIFIED)
    }
}

data class BasicSportLessonData(
    val id: Long,
    val sectionId: Long,
    val sectionName: String,
    val sectionLevel: Long,
    val level: Long,
    val typeId: Long,
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
    val isCancelled: Boolean
    val status: QueueEntryStatus
    val createdAt: OffsetDateTime
    val firstNotifiedAt: OffsetDateTime?
    val lastNotifiedAt: OffsetDateTime?
    val cancelledAt: OffsetDateTime?
    val satisfiedAt: OffsetDateTime?
    val expiredAt: OffsetDateTime?
    val notificationAttempts: Int
    val maxNotificationAttempts: Int

    /**
     * For FreeSign, this is the real lesson.
     * For AutoSign, this is the prototype lesson.
     */
    val targetLesson: BasicSportLessonData
}

data class SportFreeSignEntry(
    override val id: Long,
    val lessonId: Long,
    override val position: Int,
    override val total: Int,
    override val isCancelled: Boolean,
    override val status: QueueEntryStatus,
    override val createdAt: OffsetDateTime,
    override val firstNotifiedAt: OffsetDateTime?,
    override val lastNotifiedAt: OffsetDateTime?,
    override val cancelledAt: OffsetDateTime?,
    override val satisfiedAt: OffsetDateTime?,
    override val expiredAt: OffsetDateTime?,
    override val notificationAttempts: Int,
    override val maxNotificationAttempts: Int,
    override val targetLesson: BasicSportLessonData,
    val forceSign: Boolean,
) : QueueEntry

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
    override val isCancelled: Boolean,
    override val status: QueueEntryStatus,
    override val createdAt: OffsetDateTime,
    override val firstNotifiedAt: OffsetDateTime?,
    override val lastNotifiedAt: OffsetDateTime?,
    override val cancelledAt: OffsetDateTime?,
    override val satisfiedAt: OffsetDateTime?,
    override val expiredAt: OffsetDateTime?,
    override val notificationAttempts: Int,
    override val maxNotificationAttempts: Int,
    override val targetLesson: BasicSportLessonData,
    val realLessonData: BasicSportLessonData?,
) : QueueEntry

data class SportAutoSignQueue(
    val prototypeLessonId: Long,
    val total: Int
)

data class SportAutoSignLimits(
    val limit: Int,
    val available: Int,
    val nextAvailableAt: OffsetDateTime,
)

// endregion sport