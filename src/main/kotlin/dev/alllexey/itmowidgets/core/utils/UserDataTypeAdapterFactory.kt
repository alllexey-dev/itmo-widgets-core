package dev.alllexey.itmowidgets.core.utils

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dev.alllexey.itmowidgets.core.model.UserCapabilities
import dev.alllexey.itmowidgets.core.model.UserData

/** Gson bypasses Kotlin constructors when allocating a profile with a missing capabilities field. */
class UserDataTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != UserData::class.java) return null
        val delegate = gson.getDelegateAdapter(this, type)
        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) = delegate.write(out, value)

            override fun read(reader: JsonReader): T {
                val value = delegate.read(reader)
                requireCapabilities((value as UserData).capabilities)
                return value
            }
        }.nullSafe()
    }

    private fun requireCapabilities(capabilities: UserCapabilities?) {
        if (capabilities == null) throw JsonParseException("Missing user capabilities")
    }
}
