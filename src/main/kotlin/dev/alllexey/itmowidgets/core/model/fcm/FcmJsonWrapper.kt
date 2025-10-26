package dev.alllexey.itmowidgets.core.model.fcm

import com.google.gson.JsonElement

data class FcmJsonWrapper(
    val type: String,
    val payload: JsonElement,
)