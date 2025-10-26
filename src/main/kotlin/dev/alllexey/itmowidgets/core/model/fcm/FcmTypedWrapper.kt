package dev.alllexey.itmowidgets.core.model.fcm

data class FcmTypedWrapper<T>(
    val type: String,
    val payload: T,
)