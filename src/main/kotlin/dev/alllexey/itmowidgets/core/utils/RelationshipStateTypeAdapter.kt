package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dev.alllexey.itmowidgets.core.model.social.RelationshipState

/** Unknown or malformed states must not silently become NONE and enable a relationship action. */
class RelationshipStateTypeAdapter : TypeAdapter<RelationshipState>() {
    override fun write(out: JsonWriter, value: RelationshipState?) {
        if (value == null) throw JsonParseException("Relationship state must not be null")
        out.value(value.name)
    }

    override fun read(reader: JsonReader): RelationshipState {
        if (reader.peek() != JsonToken.STRING) throw JsonParseException("Relationship state must be a string")
        val wire = reader.nextString()
        return RelationshipState.entries.firstOrNull { it.name == wire }
            ?: throw JsonParseException("Unknown relationship state")
    }
}
