package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dev.alllexey.itmowidgets.core.model.AppVersionInfo

/** Preserves the required-string contract instead of letting Gson bypass Kotlin nullability. */
class AppVersionInfoTypeAdapter : TypeAdapter<AppVersionInfo>() {
    override fun write(out: JsonWriter, value: AppVersionInfo?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("minVersion").value(value.minVersion)
        out.name("latestVersion").value(value.latestVersion)
        out.name("note").value(value.note)
        out.endObject()
    }

    override fun read(reader: JsonReader): AppVersionInfo {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw JsonParseException("App version info must be an object")
        }
        var minVersion: String? = null
        var latestVersion: String? = null
        var note: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (val field = reader.nextName()) {
                "minVersion" -> {
                    if (minVersion != null) throw JsonParseException("Duplicate minVersion")
                    minVersion = readString(reader, field)
                }
                "latestVersion" -> {
                    if (latestVersion != null) throw JsonParseException("Duplicate latestVersion")
                    latestVersion = readString(reader, field)
                }
                "note" -> {
                    if (note != null) throw JsonParseException("Duplicate note")
                    note = readString(reader, field)
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return AppVersionInfo(
            minVersion = minVersion ?: throw JsonParseException("Missing minVersion"),
            latestVersion = latestVersion ?: throw JsonParseException("Missing latestVersion"),
            note = note ?: throw JsonParseException("Missing note")
        )
    }

    private fun readString(reader: JsonReader, field: String): String {
        if (reader.peek() != JsonToken.STRING) throw JsonParseException("$field must be a string")
        return reader.nextString()
    }
}
