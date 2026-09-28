package reviews

import api.myitmo.MyItmo
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.LessonSyncRequest
import dev.alllexey.itmowidgets.core.model.reviews.ExternalTeacherReview
import dev.alllexey.itmowidgets.core.model.reviews.TeacherReviewsResponse
import java.time.LocalDate
import java.util.UUID
import kotlin.test.*

class TeacherReviewContractTest {
    private val gson = ItmoWidgetsImpl(MyItmo()).gson
    private val review = ExternalTeacherReview(
        UUID.fromString("00000000-0000-0000-0000-000000000077"), "Тестовый предмет",
        LocalDate.of(2025, 1, 25), null, "Тестовый источник", "https://example.test/review", "Тестовый отзыв",
    )
    private val undated = review.copy(subjectTitle = null, writtenOn = null, writtenBeforeYear = null,
        sourceTitle = null, sourceLink = null)
    private val response = TeacherReviewsResponse(100001, "https://onetwozzzplus.github.io/reviews/#/teacher/100001", listOf(review))
    private val optionalFields = listOf("subjectTitle", "writtenOn", "writtenBeforeYear", "sourceTitle", "sourceLink")

    @Test
    fun `reviews and responses round trip with all supported date and null variants`() {
        val variants = listOf(review, review.copy(writtenOn = null, writtenBeforeYear = 2024), undated)
        for (value in variants) {
            assertEquals(value, gson.fromJson(gson.toJson(value), ExternalTeacherReview::class.java))
        }
        for (value in listOf(response.copy(external = variants), response.copy(external = emptyList()))) {
            assertEquals(value, gson.fromJson(gson.toJson(value), TeacherReviewsResponse::class.java))
        }
    }

    @Test
    fun `review models use exact wire keys and omit optional nulls`() {
        assertEquals(setOf("teacherIsu", "providerUrl", "external"), gson.toJsonTree(response).asJsonObject.keySet())
        assertEquals(setOf("id", "subjectTitle", "writtenOn", "sourceTitle", "sourceLink", "text"),
            gson.toJsonTree(review).asJsonObject.keySet())
        assertEquals(setOf("id", "subjectTitle", "writtenBeforeYear", "sourceTitle", "sourceLink", "text"),
            gson.toJsonTree(review.copy(writtenOn = null, writtenBeforeYear = 2024)).asJsonObject.keySet())
        assertEquals(setOf("id", "text"), gson.toJsonTree(undated).asJsonObject.keySet())
        val withNulls = gson.newBuilder().serializeNulls().create().toJsonTree(undated).asJsonObject
        assertEquals(setOf("id", "subjectTitle", "writtenOn", "writtenBeforeYear", "sourceTitle", "sourceLink", "text"),
            withNulls.keySet())
        optionalFields.forEach { assertTrue(withNulls[it].isJsonNull, it) }
    }

    @Test
    fun `Backend date strings and explicit null fields decode into typed reviews`() {
        val wire = """{"teacherIsu":100001,"providerUrl":"${response.providerUrl}","external":[
            {"id":"${review.id}","subjectTitle":"Тестовый предмет","writtenOn":"2025-01-25","writtenBeforeYear":null,
            "sourceTitle":"Тестовый источник","sourceLink":"https://example.test/review","text":"Тестовый отзыв"},
            {"id":"${review.id}","subjectTitle":null,"writtenOn":null,"writtenBeforeYear":null,
            "sourceTitle":null,"sourceLink":null,"text":"Тестовый отзыв"}]}"""

        assertEquals(response.copy(external = listOf(review, undated)), gson.fromJson(wire, TeacherReviewsResponse::class.java))
    }

    @Test
    fun `every optional review field decodes from both explicit null and absence`() {
        val explicitNulls = gson.toJsonTree(review).asJsonObject.apply { optionalFields.forEach { add(it, JsonNull.INSTANCE) } }
        assertEquals(undated, gson.fromJson(explicitNulls, ExternalTeacherReview::class.java))
        val absent = gson.toJsonTree(review).asJsonObject.apply { optionalFields.forEach { remove(it) } }
        assertEquals(undated, gson.fromJson(absent, ExternalTeacherReview::class.java))
    }

    @Test
    fun `required review and response fields never become JVM defaults`() {
        val required = listOf(
            Triple(response as Any, TeacherReviewsResponse::class.java, listOf("teacherIsu", "providerUrl", "external")),
            Triple(review as Any, ExternalTeacherReview::class.java, listOf("id", "text")),
        )
        for ((model, type, names) in required) for (name in names) {
            val missing = gson.toJsonTree(model).asJsonObject.apply { remove(name) }
            assertFailsWith<JsonParseException>("${type.simpleName}.$name missing") { gson.fromJson(missing, type) }
            val nullValue = gson.toJsonTree(model).asJsonObject.apply { add(name, JsonNull.INSTANCE) }
            assertFailsWith<JsonParseException>("${type.simpleName}.$name null") { gson.fromJson(nullValue, type) }
        }
    }

    @Test
    fun `required strings and arrays reject incompatible JSON shapes`() {
        val required = listOf(
            Triple(response as Any, TeacherReviewsResponse::class.java, listOf("providerUrl", "external")),
            Triple(review as Any, ExternalTeacherReview::class.java, listOf("id", "text")),
        )
        for ((model, type, names) in required) for (name in names) {
            for (wire in listOf("42", "true", "{}")) {
                val json = gson.toJsonTree(model).asJsonObject.apply { add(name, JsonParser.parseString(wire)) }
                assertFailsWith<JsonParseException>("${type.simpleName}.$name: $wire") { gson.fromJson(json, type) }
            }
        }
        for (wire in listOf("\"reviews\"", "[42]", "[\"review\"]")) {
            val json = gson.toJsonTree(response).asJsonObject.apply { add("external", JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>(wire) { gson.fromJson(json, TeacherReviewsResponse::class.java) }
        }
    }

    @Test
    fun `teacher Isu must be a positive JSON integer within Int range`() {
        for (wire in listOf("\"100001\"", "1.5", "0", "-1", "2147483648", "true", "[]", "{}")) {
            val json = gson.toJsonTree(response).asJsonObject.apply { add("teacherIsu", JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>(wire) { gson.fromJson(json, TeacherReviewsResponse::class.java) }
        }
    }

    @Test
    fun `optional review strings do not coerce other JSON shapes`() {
        for (field in listOf("subjectTitle", "sourceTitle", "sourceLink", "writtenOn")) {
            for (wire in listOf("42", "true", "{}", "[]")) {
                val json = gson.toJsonTree(review).asJsonObject.apply { add(field, JsonParser.parseString(wire)) }
                assertFailsWith<JsonParseException>("$field: $wire") { gson.fromJson(json, ExternalTeacherReview::class.java) }
            }
        }
    }

    @Test
    fun `review identifiers dates and years reject malformed values`() {
        val malformed = listOf(
            "id" to "\"not-a-uuid\"",
            "writtenOn" to "\"25.01.2025\"",
            "writtenOn" to "\"2025-02-30\"",
            "writtenOn" to "\"2025-01-25T10:00:00Z\"",
            "writtenBeforeYear" to "\"2024\"",
            "writtenBeforeYear" to "2024.5",
            "writtenBeforeYear" to "2147483648",
            "writtenBeforeYear" to "true",
            "writtenBeforeYear" to "{}",
            "writtenBeforeYear" to "[]",
        )
        for ((field, wire) in malformed) {
            val json = gson.toJsonTree(undated).asJsonObject.apply { add(field, JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>("$field: $wire") { gson.fromJson(json, ExternalTeacherReview::class.java) }
        }
    }

    @Test
    fun `a review cannot supply both exact date and before year`() {
        val json = gson.toJsonTree(review).asJsonObject.apply { addProperty("writtenBeforeYear", 2024) }

        assertFailsWith<JsonParseException> { gson.fromJson(json, ExternalTeacherReview::class.java) }
    }

    @Test
    fun `external review arrays reject null elements`() {
        val json = gson.toJsonTree(response).asJsonObject.apply { getAsJsonArray("external").add(JsonNull.INSTANCE) }

        assertFailsWith<JsonParseException> { gson.fromJson(json, TeacherReviewsResponse::class.java) }
    }

    @Test
    fun `duplicate keys are rejected in response and nested reviews`() {
        val duplicateResponse = gson.toJson(response).dropLast(1) + ",\"teacherIsu\":100002}"
        assertFailsWith<JsonParseException> { gson.fromJson(duplicateResponse, TeacherReviewsResponse::class.java) }
        val duplicateReview = gson.toJson(review).dropLast(1) + ",\"text\":\"Другой отзыв\"}"
        assertFailsWith<JsonParseException> { gson.fromJson(duplicateReview, ExternalTeacherReview::class.java) }
        val nested = """{"teacherIsu":100001,"providerUrl":"${response.providerUrl}","external":[$duplicateReview]}"""
        assertFailsWith<JsonParseException> { gson.fromJson(nested, TeacherReviewsResponse::class.java) }
    }

    @Test
    fun `review models reject non-object JSON roots`() {
        for (type in listOf(TeacherReviewsResponse::class.java, ExternalTeacherReview::class.java)) {
            for (wire in listOf("[]", "\"review\"", "42", "true")) {
                assertFailsWith<JsonParseException>("${type.simpleName}: $wire") { gson.fromJson(wire, type) }
            }
        }
    }

    @Test
    fun `lesson sync still serializes and decodes its date boundaries`() {
        val request = LessonSyncRequest(emptyList(), LocalDate.of(2025, 1, 20), LocalDate.of(2025, 1, 26))

        assertEquals(JsonParser.parseString("""{"lessons":[],"from":"2025-01-20","to":"2025-01-26"}"""),
            gson.toJsonTree(request))
        assertEquals(request, gson.fromJson(gson.toJson(request), LessonSyncRequest::class.java))
    }
}
