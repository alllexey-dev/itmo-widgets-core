package dev.alllexey.itmowidgets.core.model

data class ErrorDetails(
    val message: String,
    val code: String?
)

data class ItmoTokenLoginRequest(val itmoToken: String)
data class RefreshTokenRequest(val refreshToken: String)
data class LogoutRequest(val refreshToken: String)
data class RegisterDeviceRequest(val fcmToken: String, val deviceName: String)