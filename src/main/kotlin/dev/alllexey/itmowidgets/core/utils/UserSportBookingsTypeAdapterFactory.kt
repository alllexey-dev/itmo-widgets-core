package dev.alllexey.itmowidgets.core.utils

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dev.alllexey.itmowidgets.core.model.SportQueueEntry
import dev.alllexey.itmowidgets.core.model.UserSportBookingsResponse

/** Older confirmed-only servers omit entries; construct its default rather than letting Gson insert null. */
class UserSportBookingsTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != UserSportBookingsResponse::class.java) return null
        val idsAdapter = gson.getAdapter(object : TypeToken<List<Long>>() {})
        val entriesAdapter = gson.getAdapter(object : TypeToken<List<SportQueueEntry>>() {})
        val adapter = object : TypeAdapter<UserSportBookingsResponse>() {
            override fun write(out: JsonWriter, value: UserSportBookingsResponse) {
                out.beginObject()
                out.name("lessonIds")
                idsAdapter.write(out, value.lessonIds)
                out.name("entries")
                entriesAdapter.write(out, value.entries)
                out.endObject()
            }

            override fun read(reader: JsonReader): UserSportBookingsResponse {
                if (reader.peek() != JsonToken.BEGIN_OBJECT) throw JsonParseException("Bookings must be an object")
                var ids: List<Long>? = null
                var entries: List<SportQueueEntry>? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "lessonIds" -> {
                            if (ids != null || reader.peek() != JsonToken.BEGIN_ARRAY) throw JsonParseException("Invalid lessonIds")
                            ids = idsAdapter.read(reader)
                        }
                        "entries" -> {
                            if (entries != null || reader.peek() != JsonToken.BEGIN_ARRAY) throw JsonParseException("Invalid entries")
                            entries = entriesAdapter.read(reader)
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                return UserSportBookingsResponse(ids ?: throw JsonParseException("Missing lessonIds"), entries.orEmpty())
            }
        }
        @Suppress("UNCHECKED_CAST")
        return adapter.nullSafe() as TypeAdapter<T>
    }
}
