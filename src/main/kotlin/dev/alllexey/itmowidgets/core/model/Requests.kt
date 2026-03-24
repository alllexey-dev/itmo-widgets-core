package dev.alllexey.itmowidgets.core.model

import java.time.LocalDate
import java.time.LocalTime

data class RegisterDeviceRequest(val fcmToken: String, val deviceName: String)

data class IdTokenRequest(val idToken: String)

data class FriendRequest(val isu: Int)

data class LessonSyncRequest(val lessons: List<LessonData>, val from: LocalDate, val to: LocalDate)

data class LessonData(
    val date: LocalDate,

    val pairId: Long,

    val subjectId: Long?,

    val subject: String?,

    val teacherId: Long?,

    val teacherName: String?,

    val timeStart: LocalTime,
    val timeEnd: LocalTime,

    val workTypeId: Int,

    val note: String?,

    val room: String?,
    val building: String?
)

// region sport

data class SportFreeSignRequest(
    val lessonId: Long,
    val forceSign: Boolean,
)

data class SportAutoSignRequest(
    val prototypeLessonId: Long,
)

// endregion sport