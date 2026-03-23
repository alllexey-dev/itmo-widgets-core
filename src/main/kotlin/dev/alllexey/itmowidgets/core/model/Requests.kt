package dev.alllexey.itmowidgets.core.model

data class ErrorDetails(
    val message: String,
    val code: String?
)

data class RegisterDeviceRequest(val fcmToken: String, val deviceName: String)

// region sport

data class SportFreeSignRequest(
    val lessonId: Long,
    val forceSign: Boolean,
)

data class SportAutoSignRequest(
    val prototypeLessonId: Long,
)

// endregion sport