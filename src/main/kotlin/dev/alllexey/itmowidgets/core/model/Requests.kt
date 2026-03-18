package dev.alllexey.itmowidgets.core.model

data class ErrorDetails(
    val message: String,
    val code: String?
)

data class RegisterDeviceRequest(val fcmToken: String, val deviceName: String)

data class SportFilterRequest(
    val sectionIds: List<Long>,
    val buildingIds: List<Long>,
    val timeSlotIds: List<Long>,
    val teacherIds: List<Long>,
)

data class SportFreeSignRequest(
    val lessonId: Long,
    val forceSign: Boolean?,
)

data class SportAutoSignRequest(
    val prototypeLessonId: Long,
)