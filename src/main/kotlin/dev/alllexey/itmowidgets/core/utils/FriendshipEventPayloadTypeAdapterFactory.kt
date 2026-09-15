package dev.alllexey.itmowidgets.core.utils

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.fcm.impl.FriendshipEvent
import dev.alllexey.itmowidgets.core.model.fcm.impl.FriendshipEventPayload
import java.time.OffsetDateTime

/** Never let an unknown event or absent field bypass Kotlin's non-null contract. */
class FriendshipEventPayloadTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != FriendshipEventPayload::class.java) return null
        val users = gson.getAdapter(UserData::class.java)
        val times = gson.getAdapter(OffsetDateTime::class.java)
        val adapter = object : TypeAdapter<FriendshipEventPayload>() {
            override fun write(out: JsonWriter, value: FriendshipEventPayload) {
                out.beginObject()
                out.name("event").value(value.event.name)
                out.name("user")
                users.write(out, value.user)
                out.name("occurredAt")
                times.write(out, value.occurredAt)
                out.endObject()
            }

            override fun read(reader: JsonReader): FriendshipEventPayload {
                if (reader.peek() != JsonToken.BEGIN_OBJECT) throw JsonParseException("Expected friendship event object")
                var event: FriendshipEvent? = null
                var user: UserData? = null
                var occurredAt: OffsetDateTime? = null
                val fields = mutableSetOf<String>()
                reader.beginObject()
                while (reader.hasNext()) {
                    val field = reader.nextName()
                    if (!fields.add(field)) throw JsonParseException("Duplicate friendship event field")
                    when (field) {
                        "event" -> {
                            if (reader.peek() != JsonToken.STRING) throw JsonParseException("Expected friendship event string")
                            val value = reader.nextString()
                            event = FriendshipEvent.entries.firstOrNull { it.name == value }
                                ?: throw JsonParseException("Unknown friendship event")
                        }
                        "user" -> user = users.read(reader)
                        "occurredAt" -> occurredAt = times.read(reader)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                return FriendshipEventPayload(
                    event ?: throw JsonParseException("Missing friendship event"),
                    user ?: throw JsonParseException("Missing friendship actor"),
                    occurredAt ?: throw JsonParseException("Missing friendship event time"),
                )
            }
        }
        @Suppress("UNCHECKED_CAST")
        return adapter as TypeAdapter<T>
    }
}
