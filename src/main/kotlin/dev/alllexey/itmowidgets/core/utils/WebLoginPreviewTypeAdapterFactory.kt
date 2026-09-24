package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import dev.alllexey.itmowidgets.core.model.WebLoginPreview
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

class WebLoginPreviewTypeAdapterFactory : StrictResourceObjectTypeAdapterFactory(mapOf(
    WebLoginPreview::class.java to mapOf("challengeId" to 's', "createdAt" to 's', "expiresAt" to 's'),
)) {
    override fun validateJson(type: Class<*>, json: JsonObject) {
        val userAgent = json.get("userAgent")?.takeUnless { it.isJsonNull }
        if (userAgent != null && !(userAgent.isJsonPrimitive && userAgent.asJsonPrimitive.isString)) {
            throw JsonParseException("Invalid userAgent")
        }
        for (name in listOf("createdAt", "expiresAt")) {
            try { OffsetDateTime.parse(json.get(name).asString) }
            catch (error: DateTimeParseException) { throw JsonParseException("Invalid timestamp: $name", error) }
        }
    }
}
