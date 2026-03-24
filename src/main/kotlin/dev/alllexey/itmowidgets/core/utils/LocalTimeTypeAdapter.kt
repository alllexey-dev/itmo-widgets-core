package dev.alllexey.itmowidgets.core.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalTime

class LocalTimeTypeAdapter : TypeAdapter<LocalTime>() {
    override fun write(out: JsonWriter?, value: LocalTime?) {
        out?.value(value.toString())
    }

    override fun read(reader: JsonReader?): LocalTime? {
        return reader?.nextString()?.let { LocalTime.parse(it) }
    }
}