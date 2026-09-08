package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dev.alllexey.itmowidgets.core.model.SharingVisibility
import dev.alllexey.itmowidgets.core.model.UserPrivacySettings

/** Gson's default enum adapter returns null for unknown values; privacy must fail closed. */
class SharingVisibilityTypeAdapter : TypeAdapter<SharingVisibility>() {
    override fun write(out: JsonWriter, value: SharingVisibility?) {
        if (value == null) throw JsonParseException("Sharing visibility must not be null")
        out.value(value.name)
    }

    override fun read(reader: JsonReader): SharingVisibility {
        if (reader.peek() != JsonToken.STRING) {
            throw JsonParseException("Sharing visibility must be a string")
        }
        return when (reader.nextString()) {
            "ALL" -> SharingVisibility.ALL
            "FRIENDS" -> SharingVisibility.FRIENDS
            "NOBODY" -> SharingVisibility.NOBODY
            else -> throw JsonParseException("Unknown sharing visibility")
        }
    }
}

/** Constructs a complete settings value instead of allowing Gson to bypass Kotlin nullability. */
class UserPrivacySettingsTypeAdapter : TypeAdapter<UserPrivacySettings>() {
    private val visibilityAdapter = SharingVisibilityTypeAdapter()

    override fun write(out: JsonWriter, value: UserPrivacySettings?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("scheduleVisibility")
        visibilityAdapter.write(out, value.scheduleVisibility)
        out.name("sportVisibility")
        visibilityAdapter.write(out, value.sportVisibility)
        out.endObject()
    }

    override fun read(reader: JsonReader): UserPrivacySettings {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw JsonParseException("Privacy settings must be an object")
        }
        var schedule: SharingVisibility? = null
        var sport: SharingVisibility? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "scheduleVisibility" -> {
                    if (schedule != null) throw JsonParseException("Duplicate scheduleVisibility")
                    schedule = visibilityAdapter.read(reader)
                }
                "sportVisibility" -> {
                    if (sport != null) throw JsonParseException("Duplicate sportVisibility")
                    sport = visibilityAdapter.read(reader)
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return UserPrivacySettings(
            scheduleVisibility = schedule ?: throw JsonParseException("Missing scheduleVisibility"),
            sportVisibility = sport ?: throw JsonParseException("Missing sportVisibility")
        )
    }
}
