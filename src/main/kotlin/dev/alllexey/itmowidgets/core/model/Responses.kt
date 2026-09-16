package dev.alllexey.itmowidgets.core.model

import java.time.OffsetDateTime

// region user

/** Access computed by Backend for the authenticated viewer, never the owner's privacy settings. */
data class UserCapabilities @JvmOverloads constructor(
    val canViewSchedule: Boolean,
    val canViewSport: Boolean,
    val canViewFriends: Boolean = false,
)

data class UserData(
    val isu: Int,
    val name: String,
    val pictureUrl: String?,
    val groups: List<GroupData>,
    val capabilities: UserCapabilities
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

data class SportLessonDto(
    val id: Long,
    val sectionId: Long,
    val sectionName: String,
    /**
     * 1 - секции свободного посещения (или им подобные)
     * 2 - спортивные секции с отбором
     */
    val sectionLevel: Long,
    /**
     * 1 - секции свободного посещения (или им подобные)
     * 2 - Секция (обучение)
     * 3 - Секция (средний)
     * 4 - Секция (сборная)
     */
    val level: Long,
    /**
     * Имеет смысл при level = 1
     * 1 - Открытое занятие
     * 2 - Свободное посещение
     * 5 - Задолженность
     * 6 - Нормативы
     * 7 - Экстернат
     * 8 - Дополнительное
     */
    val typeId: Long,
    /** Raw venue ID, not a filter category; null is valid for online lessons. */
    val buildingId: Long?,
    val roomName: String,
    /**
     * Рекомендуется использовать start & end вместо TimeSlot
     */
    val start: OffsetDateTime,
    val end: OffsetDateTime,
    val timeSlotId: Long,
    val teacherIsu: Long,
    val teacherFio: String,
)

sealed interface SportQueueEntry {
    val type: String

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
    val targetLesson: SportLessonDto
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
    override val targetLesson: SportLessonDto,
    val forceSign: Boolean,
) : SportQueueEntry {
    override val type: String = "free"
}

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
    override val targetLesson: SportLessonDto,
    val realLesson: SportLessonDto?,
) : SportQueueEntry {
    override val type: String = "auto"
}

sealed interface SportQueue {
    val type: String
    val lessonId: Long
    val total: Int
}

data class SportFreeSignQueue(
    override val lessonId: Long,
    override val total: Int,
) : SportQueue {
    override val type: String = "free"
}

data class SportAutoSignQueue(
    override val lessonId: Long,
    override val total: Int,
    val realLessonId: Long?
) : SportQueue {
    override val type: String = "auto"
}

data class SportAutoSignLimits(
    val limit: Int,
    val available: Int,
    val nextAvailableAt: OffsetDateTime,
)

data class FriendSportBooking(
    val isu: Int,
    val lessonId: Long,
    val entry: SportQueueEntry? // null if already signed
)

data class FriendsSportBookingsResponse(
    val bookings: List<FriendSportBooking>
)

// endregion sport
