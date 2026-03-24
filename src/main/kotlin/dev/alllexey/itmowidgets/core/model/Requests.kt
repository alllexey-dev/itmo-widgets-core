package dev.alllexey.itmowidgets.core.model

data class RegisterDeviceRequest(val fcmToken: String, val deviceName: String)

data class IdTokenRequest(val idToken: String)

data class FriendRequest(val isu: Int)

// region sport

data class SportFreeSignRequest(
    val lessonId: Long,
    val forceSign: Boolean,
)

data class SportAutoSignRequest(
    val prototypeLessonId: Long,
)

// endregion sport