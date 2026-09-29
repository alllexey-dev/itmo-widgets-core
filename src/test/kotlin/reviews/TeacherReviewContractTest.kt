package reviews

import api.myitmo.MyItmo
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.LessonSyncRequest
import dev.alllexey.itmowidgets.core.model.reviews.*
import java.time.LocalDate
import kotlin.test.*

class TeacherReviewContractTest {
    private val gson = ItmoWidgetsImpl(MyItmo()).gson
    private val fixtures = TeacherReviewContractFixtures

    @Test
    fun `a Backend shaped response with named anonymous copied and own reviews decodes`() {
        val wire = """{"teacherIsu":100001,"providerUrl":"${fixtures.PROVIDER_URL}","reviews":[
            {"id":"${fixtures.namedId}","kind":"COMMUNITY","subjectTitle":"Математика","writtenOn":"2026-09-23",
             "writtenBeforeYear":null,"text":"${fixtures.TEXT}","score":3,"myVote":1,"verified":true,"reportedByMe":false,
             "author":{"isu":123456,"name":"Synthetic author","pictureUrl":null,"groups":[],
               "capabilities":{"canViewSchedule":false,"canViewSport":false,"canViewFriends":false}},
             "sourceTitle":null,"sourceLink":null},
            {"id":"${fixtures.anonymousId}","kind":"COMMUNITY","subjectTitle":null,"writtenOn":"2026-09-23",
             "writtenBeforeYear":null,"text":"${fixtures.TEXT}","score":0,"myVote":0,"verified":false,"reportedByMe":true,
             "author":null,"sourceTitle":null,"sourceLink":null},
            {"id":"${fixtures.copyId}","kind":"REVIEWS","subjectTitle":"Физика","writtenOn":null,"writtenBeforeYear":2024,
             "text":"${fixtures.TEXT}","score":-1,"myVote":-1,"verified":false,"reportedByMe":false,"author":null,
             "sourceTitle":"Тестовый источник","sourceLink":"https://example.test/review"}],
            "mine":{"id":"${fixtures.ownId}","subjectTitle":"Математика","text":"${fixtures.TEXT}","anonymous":true,
             "status":"REJECTED","reviewNote":"Не о преподавателе","score":0,"verified":false,"writtenOn":"2026-09-28"},
            "canWrite":true,"canVote":true,"canReport":true,"knownTeacher":true}"""

        assertEquals(fixtures.response, gson.fromJson(wire, TeacherReviewsResponse::class.java))
        val empty = """{"teacherIsu":100001,"providerUrl":"${fixtures.PROVIDER_URL}","reviews":[],"mine":null,
            "canWrite":false,"canVote":false,"canReport":false,"knownTeacher":false}"""
        assertEquals(TeacherReviewsResponse(fixtures.TEACHER, fixtures.PROVIDER_URL, emptyList(), null, false, false, false, false),
            gson.fromJson(empty, TeacherReviewsResponse::class.java))
    }

    @Test
    fun `every review model round trips`() {
        val values = listOf(fixtures.response, fixtures.response.copy(reviews = emptyList(), mine = null),
            fixtures.named, fixtures.anonymous, fixtures.copy,
            fixtures.copy.copy(writtenBeforeYear = null, writtenOn = LocalDate.of(2025, 1, 25)),
            fixtures.copy.copy(subjectTitle = null, writtenBeforeYear = null, sourceTitle = null, sourceLink = null),
            fixtures.mine,
            fixtures.mine.copy(subjectTitle = null, status = TeacherReviewStatus.PUBLISHED, reviewNote = null, verified = true),
            fixtures.save, SaveTeacherReviewRequest(text = fixtures.TEXT), fixtures.revision, fixtures.approved,
            fixtures.moderated, fixtures.moderated.copy(teacherName = null, shown = null,
                verification = ReviewVerification.PENDING, verifiedFlowId = null),
            fixtures.response.copy(summary = fixtures.summary), fixtures.summary,
            fixtures.summary.copy(pros = emptyList(), cons = emptyList(), tags = emptyList()),
            TeacherSummaryLevel(123456, SummaryLevel.VERY_NEGATIVE))
        for (value in values) assertEquals(value, gson.fromJson(gson.toJson(value), value.javaClass), value.javaClass.simpleName)
    }

    @Test
    fun `review models use the exact wire keys`() {
        val withNulls = gson.newBuilder().serializeNulls().create()
        assertEquals(setOf("teacherIsu", "providerUrl", "reviews", "mine", "canWrite", "canVote", "canReport", "knownTeacher",
            "summary"), withNulls.toJsonTree(fixtures.response).asJsonObject.keySet())
        assertEquals(setOf("reviewCount", "description", "pros", "cons", "tags", "scales", "level", "confidence", "generatedAt"),
            gson.toJsonTree(fixtures.summary).asJsonObject.keySet())
        assertEquals(setOf("kind", "value", "reason"), withNulls.toJsonTree(fixtures.summary.scales[2]).asJsonObject.keySet())
        assertEquals(setOf("teacherIsu", "level"), gson.toJsonTree(TeacherSummaryLevel(123456, SummaryLevel.MIXED)).asJsonObject.keySet())
        assertEquals(REVIEW_KEYS, withNulls.toJsonTree(fixtures.anonymous).asJsonObject.keySet())
        assertEquals(setOf("id", "subjectTitle", "text", "anonymous", "status", "reviewNote", "score", "verified", "writtenOn"),
            withNulls.toJsonTree(fixtures.mine).asJsonObject.keySet())
        assertEquals(setOf("id", "kind", "subjectTitle", "writtenOn", "text", "score", "myVote", "verified", "reportedByMe", "author"),
            gson.toJsonTree(fixtures.named).asJsonObject.keySet())
        assertEquals(setOf("id", "kind", "subjectTitle", "writtenBeforeYear", "text", "score", "myVote", "verified", "reportedByMe",
            "sourceTitle", "sourceLink"), gson.toJsonTree(fixtures.copy).asJsonObject.keySet())
        assertEquals(setOf("id", "reviewId", "number", "subjectTitle", "text", "status", "submittedAt", "decidedAt", "note"),
            gson.toJsonTree(fixtures.approved).asJsonObject.keySet())
        assertEquals(setOf("id", "teacherIsu", "teacherName", "anonymous", "status", "reviewNote", "shown", "score", "hidden",
            "verification", "verifiedFlowId"), withNulls.toJsonTree(fixtures.moderated).asJsonObject.keySet())
        assertEquals(JsonParser.parseString("""{"subjectTitle":"Математика","text":"${fixtures.TEXT}","anonymous":false,
            "flowIds":[93724,93725]}"""), gson.toJsonTree(fixtures.save))
    }

    @Test
    fun `a response without a summary or with a null summary has none`() {
        val base = gson.toJsonTree(fixtures.response).asJsonObject.apply { remove("summary") }
        assertNull(gson.fromJson(base, TeacherReviewsResponse::class.java).summary)
        val explicitNull = base.deepCopy().apply { add("summary", JsonNull.INSTANCE) }
        assertNull(gson.fromJson(explicitNull, TeacherReviewsResponse::class.java).summary)
    }

    @Test
    fun `a Backend shaped summary decodes with unknown tags kept as strings`() {
        val wire = gson.toJsonTree(fixtures.response).asJsonObject.apply {
            add("summary", JsonParser.parseString(fixtures.SUMMARY_JSON))
        }

        val decoded = gson.fromJson(wire, TeacherReviewsResponse::class.java)

        assertEquals(fixtures.response.copy(summary = fixtures.summary), decoded)
        assertEquals(listOf("MANY_LABS", "NEW_TAG"), decoded.summary?.tags)
        assertNull(decoded.summary?.scales?.single { it.value == SummaryScaleValue.NOT_ENOUGH_DATA }?.reason)
    }

    @Test
    fun `summaries that break the contract are rejected`() {
        val invalid = mapOf<String, JsonObject.() -> Unit>(
            "reviewCount 2" to { addProperty("reviewCount", 2) },
            "four scales" to { getAsJsonArray("scales").remove(4) },
            "six scales" to { getAsJsonArray("scales").add(getAsJsonArray("scales")[0].deepCopy()) },
            "repeated kind" to { getAsJsonArray("scales")[4].asJsonObject.addProperty("kind", "EXPLAINS") },
            "unknown kind" to { getAsJsonArray("scales")[4].asJsonObject.addProperty("kind", "HUMOUR") },
            "unknown scale value" to { getAsJsonArray("scales")[4].asJsonObject.addProperty("value", "EXTREME") },
            "scale kind as a number" to { getAsJsonArray("scales")[4].asJsonObject.addProperty("kind", 4) },
            "scale as a string" to { getAsJsonArray("scales").set(4, JsonPrimitive("WORKLOAD")) },
            "reason without data" to { getAsJsonArray("scales")[2].asJsonObject.addProperty("reason", "Мало отзывов") },
            "reason as a number" to { getAsJsonArray("scales")[0].asJsonObject.addProperty("reason", 1) },
            "unknown level" to { addProperty("level", "NEUTRAL") },
            "unknown confidence" to { addProperty("confidence", "CERTAIN") },
            "numeric tag" to { add("tags", JsonParser.parseString("[1]")) },
            "null tag" to { add("tags", JsonParser.parseString("[null]")) },
            "numeric pro" to { add("pros", JsonParser.parseString("[1]")) },
            "object con" to { add("cons", JsonParser.parseString("[{}]")) },
            "local generatedAt" to { addProperty("generatedAt", "2026-09-29T09:00:00") },
            "date generatedAt" to { addProperty("generatedAt", "2026-09-29") },
            "numeric generatedAt" to { addProperty("generatedAt", 1790000000) },
        )
        for ((name, change) in invalid) {
            val summary = JsonParser.parseString(fixtures.SUMMARY_JSON).asJsonObject.apply(change)
            assertFailsWith<JsonParseException>(name) { gson.fromJson(summary, TeacherSummary::class.java) }
            val response = gson.toJsonTree(fixtures.response).asJsonObject.apply { add("summary", summary) }
            assertFailsWith<JsonParseException>("response: $name") { gson.fromJson(response, TeacherReviewsResponse::class.java) }
        }
        for (wire in listOf("\"summary\"", "[]", "42", "true")) {
            val response = gson.toJsonTree(fixtures.response).asJsonObject.apply { add("summary", JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>("summary: $wire") { gson.fromJson(response, TeacherReviewsResponse::class.java) }
        }
    }

    @Test
    fun `summary levels are strict`() {
        assertEquals(TeacherSummaryLevel(123456, SummaryLevel.VERY_POSITIVE),
            gson.fromJson("""{"teacherIsu":123456,"level":"VERY_POSITIVE"}""", TeacherSummaryLevel::class.java))
        for (wire in listOf("""{"teacherIsu":123456,"level":"NEUTRAL"}""", """{"teacherIsu":123456,"level":null}""",
            """{"teacherIsu":123456}""", """{"level":"MIXED"}""", """{"teacherIsu":0,"level":"MIXED"}""",
            """{"teacherIsu":"123456","level":"MIXED"}""", """{"teacherIsu":123456,"level":"MIXED","level":"MIXED"}""")) {
            assertFailsWith<JsonParseException>(wire) { gson.fromJson(wire, TeacherSummaryLevel::class.java) }
        }
    }

    @Test
    fun `a default save request is anonymous without flows`() {
        assertEquals(JsonParser.parseString("""{"text":"${fixtures.TEXT}","anonymous":true,"flowIds":[]}"""),
            gson.toJsonTree(SaveTeacherReviewRequest(text = fixtures.TEXT)))
    }

    @Test
    fun `optional review fields decode from both explicit null and absence`() {
        val cases = listOf(
            Triple(fixtures.named, listOf("subjectTitle", "writtenOn", "author"),
                fixtures.named.copy(subjectTitle = null, writtenOn = null, author = null)),
            Triple(fixtures.copy, listOf("subjectTitle", "writtenBeforeYear", "sourceTitle", "sourceLink"),
                fixtures.copy.copy(subjectTitle = null, writtenBeforeYear = null, sourceTitle = null, sourceLink = null)),
            Triple(fixtures.mine, listOf("subjectTitle", "reviewNote"), fixtures.mine.copy(subjectTitle = null, reviewNote = null)),
            Triple(fixtures.response, listOf("mine"), fixtures.response.copy(mine = null)),
            Triple(fixtures.approved, listOf("subjectTitle", "decidedAt", "note"),
                fixtures.approved.copy(subjectTitle = null, decidedAt = null, note = null)),
            Triple(fixtures.moderated, listOf("teacherName", "reviewNote", "shown", "verifiedFlowId"),
                fixtures.moderated.copy(teacherName = null, shown = null, verifiedFlowId = null)),
            Triple(fixtures.save, listOf("subjectTitle"), fixtures.save.copy(subjectTitle = null)),
        )
        for ((model, names, expected) in cases) {
            val explicitNulls = gson.toJsonTree(model).asJsonObject.apply { names.forEach { add(it, JsonNull.INSTANCE) } }
            assertEquals(expected, gson.fromJson(explicitNulls, model.javaClass), model.javaClass.simpleName)
            val absent = gson.toJsonTree(model).asJsonObject.apply { names.forEach { remove(it) } }
            assertEquals(expected, gson.fromJson(absent, model.javaClass), model.javaClass.simpleName)
        }
    }

    @Test
    fun `required fields never become JVM defaults`() {
        val required = listOf(
            fixtures.response to listOf("teacherIsu", "providerUrl", "reviews", "canWrite", "canVote", "canReport", "knownTeacher"),
            fixtures.named to listOf("id", "kind", "text", "score", "myVote", "verified", "reportedByMe"),
            fixtures.mine to listOf("id", "text", "anonymous", "status", "score", "verified", "writtenOn"),
            fixtures.save to listOf("text", "anonymous", "flowIds"),
            fixtures.revision to listOf("id", "reviewId", "number", "text", "status", "submittedAt"),
            fixtures.moderated to listOf("id", "teacherIsu", "anonymous", "status", "score", "hidden", "verification"),
            fixtures.summary to listOf("reviewCount", "description", "pros", "cons", "tags", "scales", "level", "confidence",
                "generatedAt"),
            fixtures.summary.scales[0] to listOf("kind", "value"),
            TeacherSummaryLevel(123456, SummaryLevel.MIXED) to listOf("teacherIsu", "level"),
        )
        for ((model, names) in required) for (name in names) {
            val type = model.javaClass
            val missing = gson.toJsonTree(model).asJsonObject.apply { remove(name) }
            assertFailsWith<JsonParseException>("${type.simpleName}.$name missing") { gson.fromJson(missing, type) }
            val nullValue = gson.toJsonTree(model).asJsonObject.apply { add(name, JsonNull.INSTANCE) }
            assertFailsWith<JsonParseException>("${type.simpleName}.$name null") { gson.fromJson(nullValue, type) }
        }
    }

    @Test
    fun `required values reject incompatible JSON shapes`() {
        val malformed = listOf(
            fixtures.response to listOf("teacherIsu" to "\"100001\"", "teacherIsu" to "0", "teacherIsu" to "-1",
                "teacherIsu" to "2147483648", "providerUrl" to "42", "reviews" to "{}", "reviews" to "[null]", "reviews" to "[42]",
                "canWrite" to "\"true\"", "knownTeacher" to "1", "mine" to "\"mine\""),
            fixtures.named to listOf("id" to "\"not-a-uuid\"", "myVote" to "2", "myVote" to "-2", "score" to "1.5",
                "score" to "\"3\"", "verified" to "1", "reportedByMe" to "\"false\"", "text" to "42", "author" to "\"author\""),
            fixtures.mine to listOf("id" to "\"not-a-uuid\"", "anonymous" to "1", "writtenOn" to "\"28.09.2026\"",
                "writtenOn" to "\"2026-09-28T10:00:00Z\"", "score" to "true"),
            fixtures.save to listOf("anonymous" to "1", "anonymous" to "\"true\"", "flowIds" to "[null]", "flowIds" to "[\"93724\"]",
                "flowIds" to "[1.5]", "flowIds" to "[9223372036854775808]", "text" to "42"),
            fixtures.revision to listOf("id" to "\"not-a-uuid\"", "reviewId" to "\"not-a-uuid\"", "number" to "0",
                "submittedAt" to "42"),
            fixtures.moderated to listOf("id" to "\"not-a-uuid\"", "teacherIsu" to "0", "hidden" to "\"false\"",
                "verifiedFlowId" to "\"93724\"", "verifiedFlowId" to "1.5", "shown" to "\"shown\""),
        )
        for ((model, fields) in malformed) for ((field, wire) in fields) {
            val type = model.javaClass
            val json = gson.toJsonTree(model).asJsonObject.apply { add(field, JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>("${type.simpleName}.$field: $wire") { gson.fromJson(json, type) }
        }
    }

    @Test
    fun `optional review strings do not coerce other JSON shapes`() {
        val optional = listOf(
            fixtures.named to listOf("subjectTitle", "writtenOn"),
            fixtures.copy to listOf("sourceTitle", "sourceLink"),
            fixtures.mine to listOf("subjectTitle", "reviewNote"),
            fixtures.save to listOf("subjectTitle"),
            fixtures.approved to listOf("subjectTitle", "note"),
            fixtures.moderated to listOf("teacherName", "reviewNote"),
        )
        for ((model, names) in optional) for (name in names) for (wire in listOf("42", "true", "{}", "[]")) {
            val type = model.javaClass
            val json = gson.toJsonTree(model).asJsonObject.apply { add(name, JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>("${type.simpleName}.$name: $wire") { gson.fromJson(json, type) }
        }
    }

    @Test
    fun `review kinds carry only their own fields and at most one date`() {
        val invalid = listOf(
            review(fixtures.copy) { add("author", gson.toJsonTree(fixtures.author)) },
            review(fixtures.copy) { addProperty("verified", true) },
            review(fixtures.copy) { addProperty("reportedByMe", true) },
            review(fixtures.named) { addProperty("sourceLink", "https://example.test/review") },
            review(fixtures.named) { addProperty("sourceTitle", "Тестовый источник") },
            review(fixtures.anonymous) { remove("writtenOn"); addProperty("writtenBeforeYear", 2024) },
            review(fixtures.named) { addProperty("writtenBeforeYear", 2024) },
            review(fixtures.copy) { addProperty("writtenOn", "2025-01-25") },
            review(fixtures.copy) { addProperty("writtenBeforeYear", "2024") },
        )
        for (json in invalid) assertFailsWith<JsonParseException>(json.toString()) { gson.fromJson(json, TeacherReview::class.java) }
    }

    @Test
    fun `review enums are strict`() {
        for (type in listOf(TeacherReviewKind::class.java, TeacherReviewStatus::class.java, ReviewRevisionStatus::class.java,
            ReviewVerification::class.java, SummaryLevel::class.java, SummaryConfidence::class.java, SummaryScaleKind::class.java,
            SummaryScaleValue::class.java)) {
            for (value in type.enumConstants) assertEquals(value, gson.fromJson(gson.toJson(value), type))
            for (wire in listOf("\"UNKNOWN\"", "null", "0", "true", "{}", "[]")) {
                assertFailsWith<JsonParseException>("${type.simpleName}: $wire") { gson.fromJson(wire, type) }
            }
        }
        val fields = listOf(
            fixtures.named to ("kind" to "\"EXTERNAL\""),
            fixtures.mine to ("status" to "\"DELETED\""),
            fixtures.revision to ("status" to "\"DRAFT\""),
            fixtures.moderated to ("status" to "\"UNKNOWN\""),
            fixtures.moderated to ("verification" to "\"FAILED\""),
        )
        for ((model, field) in fields) {
            val json = gson.toJsonTree(model).asJsonObject.apply { add(field.first, JsonParser.parseString(field.second)) }
            assertFailsWith<JsonParseException>("${model.javaClass.simpleName}.${field.first}") { gson.fromJson(json, model.javaClass) }
        }
    }

    @Test
    fun `duplicate keys are rejected in the response and nested models`() {
        val duplicateResponse = gson.toJson(fixtures.response).dropLast(1) + ",\"canWrite\":false}"
        assertFailsWith<JsonParseException> { gson.fromJson(duplicateResponse, TeacherReviewsResponse::class.java) }
        val duplicateReview = gson.toJson(fixtures.anonymous).dropLast(1) + ",\"text\":\"Другой отзыв\"}"
        assertFailsWith<JsonParseException> { gson.fromJson(duplicateReview, TeacherReview::class.java) }
        val base = gson.toJsonTree(fixtures.response).asJsonObject.apply { remove("reviews"); remove("mine") }.toString().dropLast(1)
        assertFailsWith<JsonParseException> {
            gson.fromJson("$base,\"reviews\":[$duplicateReview]}", TeacherReviewsResponse::class.java)
        }
        val duplicateMine = gson.toJson(fixtures.mine).dropLast(1) + ",\"anonymous\":false}"
        assertFailsWith<JsonParseException> {
            gson.fromJson("$base,\"reviews\":[],\"mine\":$duplicateMine}", TeacherReviewsResponse::class.java)
        }
    }

    @Test
    fun `review models reject non-object JSON roots`() {
        for (type in listOf(TeacherReviewsResponse::class.java, TeacherReview::class.java, OwnTeacherReview::class.java,
            SaveTeacherReviewRequest::class.java, TeacherReviewRevision::class.java, ModeratedTeacherReview::class.java,
            TeacherSummary::class.java, TeacherSummaryScale::class.java, TeacherSummaryLevel::class.java)) {
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

    private fun review(model: TeacherReview, change: JsonObject.() -> Unit) = gson.toJsonTree(model).asJsonObject.apply(change)

    private companion object {
        val REVIEW_KEYS = setOf("id", "kind", "subjectTitle", "writtenOn", "writtenBeforeYear", "text", "score", "myVote", "verified",
            "reportedByMe", "author", "sourceTitle", "sourceLink")
    }
}
