package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/** Never accept enum ordinals, nulls, or silent defaults for access-affecting values. */
open class StrictResourceEnumTypeAdapter<T : Enum<T>>(
    private val values: List<T>,
    private val unknown: T? = null,
) : TypeAdapter<T>() {
    override fun read(reader: JsonReader): T {
        if (reader.peek() != JsonToken.STRING) throw JsonParseException("Expected enum name string")
        val name = reader.nextString()
        return values.firstOrNull { it.name == name } ?: unknown ?: throw JsonParseException("Unknown enum value")
    }
    override fun write(writer: JsonWriter, value: T?) {
        if (value == null) throw JsonParseException("Enum value must not be null")
        writer.value(value.name)
    }
}
