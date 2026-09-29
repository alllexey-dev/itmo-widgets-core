package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import dev.alllexey.itmowidgets.core.model.reviews.*
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

class TeacherReviewModelsTypeAdapterFactory : StrictResourceObjectTypeAdapterFactory(mapOf(
    TeacherReviewsResponse::class.java to mapOf("teacherIsu" to 'i', "providerUrl" to 's', "reviews" to 'a', "canWrite" to 'b',
        "canVote" to 'b', "canReport" to 'b', "knownTeacher" to 'b'),
    TeacherReview::class.java to mapOf("id" to 's', "kind" to 's', "text" to 's', "score" to 'i', "myVote" to 'i',
        "verified" to 'b', "reportedByMe" to 'b'),
    OwnTeacherReview::class.java to mapOf("id" to 's', "text" to 's', "anonymous" to 'b', "status" to 's', "score" to 'i',
        "verified" to 'b', "writtenOn" to 's'),
    SaveTeacherReviewRequest::class.java to mapOf("text" to 's', "anonymous" to 'b', "flowIds" to 'a'),
    TeacherReviewRevision::class.java to mapOf("id" to 's', "reviewId" to 's', "number" to 'i', "text" to 's', "status" to 's',
        "submittedAt" to 's'),
    ModeratedTeacherReview::class.java to mapOf("id" to 's', "teacherIsu" to 'i', "anonymous" to 'b', "status" to 's',
        "score" to 'i', "hidden" to 'b', "verification" to 's'),
)) {
    override fun validateJson(type: Class<*>, json: JsonObject) {
        when (type) {
            TeacherReviewsResponse::class.java -> positiveIsu(json)
            TeacherReview::class.java -> validateReview(json)
            OwnTeacherReview::class.java -> {
                uuid(json, "id")
                optionalStrings(json, "subjectTitle", "reviewNote")
                isoDate(json, "writtenOn")
            }
            SaveTeacherReviewRequest::class.java -> {
                optionalStrings(json, "subjectTitle")
                if (json.getAsJsonArray("flowIds").any { !isLong(it) }) throw JsonParseException("Invalid flowIds")
            }
            TeacherReviewRevision::class.java -> {
                uuid(json, "id")
                uuid(json, "reviewId")
                if (json.get("number").asInt < 1) throw JsonParseException("Invalid revision number")
                optionalStrings(json, "subjectTitle", "decidedAt", "note")
            }
            ModeratedTeacherReview::class.java -> {
                uuid(json, "id")
                positiveIsu(json)
                optionalStrings(json, "teacherName", "reviewNote")
                if (present(json, "verifiedFlowId")?.let(::isLong) == false) throw JsonParseException("Invalid verifiedFlowId")
            }
        }
    }

    /** Only named own reviews carry an author; copies from Reviews are never verified or reportable. */
    private fun validateReview(json: JsonObject) {
        uuid(json, "id")
        optionalStrings(json, "subjectTitle", "sourceTitle", "sourceLink", "writtenOn")
        if (present(json, "author")?.isJsonObject == false) throw JsonParseException("Invalid author")
        if (json.get("myVote").asInt !in -1..1) throw JsonParseException("Invalid vote")
        val writtenOn = present(json, "writtenOn")
        if (writtenOn != null) isoDate(json, "writtenOn")
        val writtenBeforeYear = present(json, "writtenBeforeYear")
        if (writtenBeforeYear != null && !isInt(writtenBeforeYear)) throw JsonParseException("Invalid writtenBeforeYear")
        if (writtenOn != null && writtenBeforeYear != null) throw JsonParseException("Review date fields are mutually exclusive")
        when (json.get("kind").asString) {
            TeacherReviewKind.COMMUNITY.name -> if (listOf("sourceTitle", "sourceLink", "writtenBeforeYear").any { present(json, it) != null }) {
                throw JsonParseException("A community review has no source")
            }
            TeacherReviewKind.REVIEWS.name -> if (present(json, "author") != null || json.get("verified").asBoolean ||
                json.get("reportedByMe").asBoolean) {
                throw JsonParseException("A copied review has no author, verification or reports")
            }
        }
    }

    private fun present(json: JsonObject, name: String): JsonElement? = json.get(name)?.takeUnless { it.isJsonNull }

    private fun positiveIsu(json: JsonObject) {
        if (json.get("teacherIsu").asInt <= 0) throw JsonParseException("Invalid teacherIsu")
    }

    private fun uuid(json: JsonObject, name: String) {
        try { UUID.fromString(json.get(name).asString) }
        catch (error: IllegalArgumentException) { throw JsonParseException("Invalid $name", error) }
    }

    private fun isoDate(json: JsonObject, name: String) {
        try { LocalDate.parse(json.get(name).asString) }
        catch (error: DateTimeParseException) { throw JsonParseException("Invalid $name", error) }
    }

    private fun optionalStrings(json: JsonObject, vararg names: String) {
        for (name in names) {
            val value = present(json, name)
            if (value != null && !(value.isJsonPrimitive && value.asJsonPrimitive.isString)) throw JsonParseException("Invalid $name")
        }
    }

    private fun isInt(value: JsonElement) = isInteger(value) && runCatching { value.asBigDecimal.intValueExact() }.isSuccess

    private fun isLong(value: JsonElement) = isInteger(value) && runCatching { value.asBigDecimal.longValueExact() }.isSuccess

    private fun isInteger(value: JsonElement) =
        value.isJsonPrimitive && value.asJsonPrimitive.isNumber && value.toString().matches(INTEGER)

    private companion object {
        val INTEGER = Regex("-?[0-9]+")
    }
}
