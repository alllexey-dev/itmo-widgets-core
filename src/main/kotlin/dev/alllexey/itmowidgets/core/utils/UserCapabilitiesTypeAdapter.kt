package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dev.alllexey.itmowidgets.core.model.UserCapabilities

/** Viewer permissions require actual JSON booleans; missing or coerced values never grant access. */
class UserCapabilitiesTypeAdapter : TypeAdapter<UserCapabilities>() {
    override fun write(out: JsonWriter, value: UserCapabilities?) {
        if (value == null) throw JsonParseException("User capabilities must not be null")
        out.beginObject()
        out.name("canViewSchedule").value(value.canViewSchedule)
        out.name("canViewSport").value(value.canViewSport)
        out.name("canViewFriends").value(value.canViewFriends)
        out.endObject()
    }

    override fun read(reader: JsonReader): UserCapabilities {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw JsonParseException("User capabilities must be an object")
        }
        var schedule: Boolean? = null
        var sport: Boolean? = null
        var friends: Boolean? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "canViewSchedule" -> {
                    if (schedule != null) throw JsonParseException("Duplicate canViewSchedule")
                    schedule = readBoolean(reader)
                }
                "canViewSport" -> {
                    if (sport != null) throw JsonParseException("Duplicate canViewSport")
                    sport = readBoolean(reader)
                }
                "canViewFriends" -> {
                    if (friends != null) throw JsonParseException("Duplicate canViewFriends")
                    friends = readBoolean(reader)
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return UserCapabilities(
            canViewSchedule = schedule ?: throw JsonParseException("Missing canViewSchedule"),
            canViewSport = sport ?: throw JsonParseException("Missing canViewSport"),
            canViewFriends = friends ?: throw JsonParseException("Missing canViewFriends"),
        )
    }

    private fun readBoolean(reader: JsonReader): Boolean {
        if (reader.peek() != JsonToken.BOOLEAN) {
            throw JsonParseException("User capability must be a boolean")
        }
        return reader.nextBoolean()
    }
}
