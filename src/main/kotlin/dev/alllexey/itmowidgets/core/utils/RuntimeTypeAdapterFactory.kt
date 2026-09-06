package dev.alllexey.itmowidgets.core.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.util.LinkedHashMap
import kotlin.collections.iterator

/**
 * Adapts values whose runtime type may differ from their declaration type.
 *
 * Kotlin version of GSON's RuntimeTypeAdapterFactory
 */
class RuntimeTypeAdapterFactory<T> private constructor(
    private val baseType: Class<*>,
    private val typeFieldName: String,
    private val maintainType: Boolean
) : TypeAdapterFactory {

    private val labelToSubtype: MutableMap<String, Class<out T>> = LinkedHashMap()
    private val subtypeToLabel: MutableMap<Class<out T>, String> = LinkedHashMap()
    private var recognizeSubtypes: Boolean = false

    companion object {
        @JvmStatic
        fun <T> of(
            baseType: Class<T>,
            typeFieldName: String,
            maintainType: Boolean
        ): RuntimeTypeAdapterFactory<T> {
            return RuntimeTypeAdapterFactory(baseType, typeFieldName, maintainType)
        }

        @JvmStatic
        fun <T> of(
            baseType: Class<T>,
            typeFieldName: String
        ): RuntimeTypeAdapterFactory<T> {
            return RuntimeTypeAdapterFactory(baseType, typeFieldName, false)
        }

        @JvmStatic
        fun <T> of(baseType: Class<T>): RuntimeTypeAdapterFactory<T> {
            return RuntimeTypeAdapterFactory(baseType, "type", false)
        }
    }

    /**
     * Ensures that this factory will handle not just the given base type, but any subtype of it.
     */
    fun recognizeSubtypes(): RuntimeTypeAdapterFactory<T> {
        recognizeSubtypes = true
        return this
    }

    /**
     * Registers [type] identified by [label].
     */
    fun registerSubtype(type: Class<out T>, label: String): RuntimeTypeAdapterFactory<T> {
        requireNotNull(type)
        requireNotNull(label)

        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw IllegalArgumentException("types and labels must be unique")
        }

        labelToSubtype[label] = type
        subtypeToLabel[type] = label
        return this
    }

    /**
     * Registers [type] identified by its simple name.
     */
    fun registerSubtype(type: Class<out T>): RuntimeTypeAdapterFactory<T> {
        return registerSubtype(type, type.simpleName)
    }

    override fun <R> create(gson: Gson, type: TypeToken<R>): TypeAdapter<R>? {
        val rawType = type.rawType
        val handle = if (recognizeSubtypes) {
            baseType.isAssignableFrom(rawType)
        } else {
            baseType == rawType
        }

        if (!handle) return null

        val jsonElementAdapter = gson.getAdapter(JsonElement::class.java)
        val labelToDelegate = LinkedHashMap<String, TypeAdapter<*>>()
        val subtypeToDelegate = LinkedHashMap<Class<*>, TypeAdapter<*>>()

        for ((label, subtype) in labelToSubtype) {
            val delegate = gson.getDelegateAdapter(this, TypeToken.get(subtype))
            labelToDelegate[label] = delegate
            subtypeToDelegate[subtype] = delegate
        }

        return object : TypeAdapter<R>() {
            @Throws(IOException::class)
            override fun read(`in`: JsonReader): R {
                val jsonElement = jsonElementAdapter.read(`in`)
                val labelJsonElement = if (maintainType) {
                    jsonElement.asJsonObject.get(typeFieldName)
                } else {
                    jsonElement.asJsonObject.remove(typeFieldName)
                }

                if (labelJsonElement == null) {
                    throw JsonParseException(
                        "cannot deserialize $baseType because it does not define a field named $typeFieldName"
                    )
                }

                val label = labelJsonElement.asString
                @Suppress("UNCHECKED_CAST")
                val delegate = labelToDelegate[label] as TypeAdapter<R>?
                    ?: throw JsonParseException(
                        "cannot deserialize $baseType subtype named $label; did you forget to register a subtype?"
                    )

                return delegate.fromJsonTree(jsonElement)
            }

            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: R) {
                val srcType: Class<*> = value!!::class.java
                @Suppress("UNCHECKED_CAST")
                val label = subtypeToLabel[srcType as Class<out T>]
                @Suppress("UNCHECKED_CAST")
                val delegate = subtypeToDelegate[srcType] as TypeAdapter<R>?
                    ?: throw JsonParseException(
                        "cannot serialize ${srcType.name}; did you forget to register a subtype?"
                    )

                val jsonObject = delegate.toJsonTree(value).asJsonObject

                if (maintainType) {
                    jsonElementAdapter.write(out, jsonObject)
                    return
                }

                if (jsonObject.has(typeFieldName)) {
                    throw JsonParseException(
                        "cannot serialize ${srcType.name} because it already defines a field named $typeFieldName"
                    )
                }

                val clone = JsonObject()
                clone.add(typeFieldName, JsonPrimitive(label))

                for ((key, element) in jsonObject.entrySet()) {
                    clone.add(key, element)
                }

                jsonElementAdapter.write(out, clone)
            }
        }.nullSafe()
    }
}
