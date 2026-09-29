package reviews

import api.myitmo.MyItmo
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.resources.*
import dev.alllexey.itmowidgets.core.model.reviews.ReviewVerification
import java.util.concurrent.TimeUnit
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import resources.ResourceContractFixtures

class TeacherReviewModerationContractTest {
    private val gson = ItmoWidgetsImpl(MyItmo()).gson
    private val fixtures = TeacherReviewContractFixtures

    @Test
    fun `a Backend shaped TEACHER_REVIEW case decodes into a review target`() {
        val wire = """{"id":"${fixtures.caseId}","targetType":"TEACHER_REVIEW","status":"OPEN","reason":"SUBMISSION",
            "openedAt":"2026-09-29T09:00:00Z","target":{"targetType":"TEACHER_REVIEW",
             "revision":{"id":"${fixtures.revisionId}","reviewId":"${fixtures.ownId}","number":2,"subjectTitle":"Математика",
              "text":"${fixtures.TEXT}","status":"PENDING","submittedAt":"2026-09-29T09:00:00Z","decidedAt":null,"note":null},
             "review":{"id":"${fixtures.ownId}","teacherIsu":100001,"teacherName":"Синтетический преподаватель","anonymous":true,
              "status":"PENDING","reviewNote":null,
              "shown":{"id":"${fixtures.approvedId}","reviewId":"${fixtures.ownId}","number":1,"subjectTitle":"Математика",
               "text":"${fixtures.TEXT}","status":"APPROVED","submittedAt":"2026-09-29T09:00:00Z",
               "decidedAt":"2026-09-29T09:00:00Z","note":"Проверено"},
              "score":2,"hidden":false,"verification":"VERIFIED","verifiedFlowId":93724},
             "author":{"isu":123456,"name":"Synthetic author","pictureUrl":null,"groups":[],
              "capabilities":{"canViewSchedule":false,"canViewSport":false,"canViewFriends":false}},
             "reports":[{"reason":"OFFENSIVE","comment":"Грубо","createdAt":"2026-09-29T09:00:00Z"}],
             "submitterHistory":{"approved":1,"rejected":0,"dismissedReports":0,"activeRestrictions":[]}},
            "decisions":[]}"""

        val case = gson.fromJson(wire, ModerationCase::class.java)

        assertEquals(fixtures.case, case)
        assertIs<TeacherReviewTarget>(case.target)
    }

    @Test
    fun `a review target round trips polymorphically with exact keys`() {
        val json = gson.toJson(fixtures.target, ModerationCaseTarget::class.java)
        assertEquals("TEACHER_REVIEW", JsonParser.parseString(json).asJsonObject["targetType"].asString)
        assertEquals(setOf("targetType", "revision", "review", "author", "reports", "submitterHistory"),
            JsonParser.parseString(json).asJsonObject.keySet())
        assertEquals(fixtures.target, assertIs<TeacherReviewTarget>(gson.fromJson(json, ModerationCaseTarget::class.java)))
        assertEquals(fixtures.case, gson.fromJson(gson.toJson(fixtures.case), ModerationCase::class.java))
        assertEquals(fixtures.case.copy(target = null), gson.fromJson(gson.toJson(fixtures.case.copy(target = null)), ModerationCase::class.java))
    }

    @Test
    fun `a review not yet approved has no shown revision`() {
        val firstVersion = fixtures.target.copy(review = fixtures.moderated.copy(shown = null,
            verification = ReviewVerification.PENDING, verifiedFlowId = null))
        val json = JsonParser.parseString(gson.toJson(firstVersion, ModerationCaseTarget::class.java)).asJsonObject
        json.getAsJsonObject("review").add("shown", JsonNull.INSTANCE)

        assertEquals(firstVersion, gson.fromJson(json, ModerationCaseTarget::class.java))
    }

    @Test
    fun `a review target requires every part and rejects a link shaped body`() {
        val json = JsonParser.parseString(gson.toJson(fixtures.target, ModerationCaseTarget::class.java)).asJsonObject
        for (name in listOf("revision", "review", "author", "reports", "submitterHistory")) {
            val missing = json.deepCopy().apply { remove(name) }
            assertFailsWith<JsonParseException>("$name missing") { gson.fromJson(missing, ModerationCaseTarget::class.java) }
            val nullValue = json.deepCopy().apply { add(name, JsonNull.INSTANCE) }
            assertFailsWith<JsonParseException>("$name null") { gson.fromJson(nullValue, ModerationCaseTarget::class.java) }
        }
        val linkTarget = gson.toJson(ResourceContractFixtures.target, ModerationCaseTarget::class.java)
        assertFailsWith<JsonParseException> {
            gson.fromJson(linkTarget.replace("SUBJECT_RESOURCE", "TEACHER_REVIEW"), ModerationCaseTarget::class.java)
        }
    }

    @Test
    fun `review report reasons decode and unknown reasons fail`() {
        for (reason in listOf(ReportReason.OFFENSIVE, ReportReason.WRONG_TEACHER)) {
            assertEquals(reason, gson.fromJson("\"${reason.name}\"", ReportReason::class.java))
            val request = ModerationReportRequest(reason, "Комментарий")
            assertEquals(request, gson.fromJson(gson.toJson(request), ModerationReportRequest::class.java))
            val report = fixtures.report.copy(reason = reason)
            assertEquals(report, gson.fromJson(gson.toJson(report), ModerationReport::class.java))
        }
        assertFailsWith<JsonParseException> { gson.fromJson("\"RUDE\"", ReportReason::class.java) }
        assertFailsWith<JsonParseException> { gson.fromJson("""{"reason":"RUDE"}""", ModerationReportRequest::class.java) }
    }

    @Test
    fun `moderationCases returns link and review cases`() = MockWebServer().use { server ->
        val client = object : ItmoWidgetsImpl(MyItmo(), server.url("/").toString()) {
            override fun getValidToken(): String? = null
        }
        val cases = listOf(ResourceContractFixtures.case, fixtures.case)
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json")
            .setBody("""{"success":true,"data":${client.gson.toJson(cases)},"error":null}"""))

        val result = runBlocking { client.moderationApi.moderationCases() }

        assertTrue(result.success)
        assertEquals(cases, result.data)
        assertIs<SubjectLinkTarget>(result.data!![0].target)
        assertIs<TeacherReviewTarget>(result.data!![1].target)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/moderation/cases?status=OPEN", request.path)
    }
}
