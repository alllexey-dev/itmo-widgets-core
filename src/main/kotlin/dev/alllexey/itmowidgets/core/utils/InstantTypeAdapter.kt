package dev.alllexey.itmowidgets.core.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Instant

class InstantTypeAdapter : TypeAdapter<Instant>() {
    override fun write(out: JsonWriter?, value: Instant?) {
        out?.value(value.toString())
    }

    override fun read(reader: JsonReader?): Instant? {
        return reader?.nextString()?.let { Instant.parse(it) }
    }
}