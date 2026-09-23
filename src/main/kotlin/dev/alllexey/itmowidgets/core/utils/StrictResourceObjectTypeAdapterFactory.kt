package dev.alllexey.itmowidgets.core.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.math.BigDecimal

/** Validate required JSON shapes before Gson reflection can substitute JVM zero/null defaults. */
open class StrictResourceObjectTypeAdapterFactory(
    private val schemas: Map<Class<*>, Map<String, Char>>,
) : TypeAdapterFactory {
    protected open fun validateJson(type: Class<*>, json: JsonObject) = Unit

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val fields = schemas[type.rawType] ?: return null
        val delegate = gson.getDelegateAdapter(this, type)
        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T) { delegate.write(out, value) }
            override fun read(reader: JsonReader): T {
                val tree = readTree(reader)
                if (!tree.isJsonObject) throw JsonParseException("Expected object")
                val json = tree.asJsonObject
                for ((name, kind) in fields) {
                    val value = json.get(name) ?: throw JsonParseException("Missing required field: $name")
                    val valid = when (kind) {
                        's' -> value.isJsonPrimitive && value.asJsonPrimitive.isString
                        'b' -> value.isJsonPrimitive && value.asJsonPrimitive.isBoolean
                        'n', 'i' -> value.isJsonPrimitive && value.asJsonPrimitive.isNumber &&
                            value.toString().matches(Regex("-?[0-9]+")) && try {
                                if (kind == 'i') value.asBigDecimal.intValueExact() else value.asBigDecimal.longValueExact()
                                true
                            } catch (_: ArithmeticException) { false }
                        'a' -> value.isJsonArray && value.asJsonArray.none { it.isJsonNull }
                        'o' -> value.isJsonObject
                        'm' -> value.isJsonObject && value.asJsonObject.entrySet().all { it.value.isJsonObject }
                        else -> false
                    }
                    if (!valid) throw JsonParseException("Invalid required field: $name")
                }
                validateJson(type.rawType, json)
                return try { delegate.fromJsonTree(json) }
                catch (error: IllegalArgumentException) { throw JsonParseException("Invalid resource model", error) }
            }

            // Parsing recursively here retains duplicate-key rejection, including nested author/policy objects.
            private fun readTree(reader: JsonReader): JsonElement = when (reader.peek()) {
                JsonToken.BEGIN_OBJECT -> JsonObject().also { json ->
                    reader.beginObject()
                    while (reader.hasNext()) {
                        val name = reader.nextName()
                        if (json.has(name)) throw JsonParseException("Duplicate field: $name")
                        json.add(name, readTree(reader))
                    }
                    reader.endObject()
                }
                JsonToken.BEGIN_ARRAY -> JsonArray().also { array ->
                    reader.beginArray()
                    while (reader.hasNext()) array.add(readTree(reader))
                    reader.endArray()
                }
                JsonToken.STRING -> JsonPrimitive(reader.nextString())
                JsonToken.NUMBER -> JsonPrimitive(BigDecimal(reader.nextString()))
                JsonToken.BOOLEAN -> JsonPrimitive(reader.nextBoolean())
                JsonToken.NULL -> { reader.nextNull(); JsonNull.INSTANCE }
                else -> throw JsonParseException("Invalid JSON value")
            }
        }.nullSafe()
    }
}
