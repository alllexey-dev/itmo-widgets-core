package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import dev.alllexey.itmowidgets.core.model.reviews.ExternalTeacherReview
import dev.alllexey.itmowidgets.core.model.reviews.TeacherReviewsResponse
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

class TeacherReviewModelsTypeAdapterFactory : StrictResourceObjectTypeAdapterFactory(mapOf(
    TeacherReviewsResponse::class.java to mapOf("teacherIsu" to 'i', "providerUrl" to 's', "external" to 'a'),
    ExternalTeacherReview::class.java to mapOf("id" to 's', "text" to 's'),
)) {
    override fun validateJson(type: Class<*>, json: JsonObject) {
        when (type) {
            TeacherReviewsResponse::class.java -> {
                if (json.get("teacherIsu").asInt <= 0) throw JsonParseException("Invalid teacherIsu")
            }
            ExternalTeacherReview::class.java -> validateReview(json)
        }
    }

    private fun validateReview(json: JsonObject) {
        try { UUID.fromString(json.get("id").asString) }
        catch (error: IllegalArgumentException) { throw JsonParseException("Invalid review id", error) }
        for (name in listOf("subjectTitle", "sourceTitle", "sourceLink", "writtenOn")) {
            val value = json.get(name)?.takeUnless { it.isJsonNull }
            if (value != null && !(value.isJsonPrimitive && value.asJsonPrimitive.isString)) {
                throw JsonParseException("Invalid $name")
            }
        }
        val writtenOn = json.get("writtenOn")?.takeUnless { it.isJsonNull }
        if (writtenOn != null) {
            try { LocalDate.parse(writtenOn.asString) }
            catch (error: DateTimeParseException) { throw JsonParseException("Invalid writtenOn", error) }
        }
        val writtenBeforeYear = json.get("writtenBeforeYear")?.takeUnless { it.isJsonNull }
        if (writtenBeforeYear != null && !(writtenBeforeYear.isJsonPrimitive && writtenBeforeYear.asJsonPrimitive.isNumber &&
                writtenBeforeYear.toString().matches(INTEGER) &&
                runCatching { writtenBeforeYear.asBigDecimal.intValueExact() }.isSuccess)) {
            throw JsonParseException("Invalid writtenBeforeYear")
        }
        if (writtenOn != null && writtenBeforeYear != null) {
            throw JsonParseException("Review date fields are mutually exclusive")
        }
    }

    private companion object {
        val INTEGER = Regex("-?[0-9]+")
    }
}
