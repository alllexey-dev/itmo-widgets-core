package reviews

import api.myitmo.MyItmo
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.ApiResponse
import dev.alllexey.itmowidgets.core.model.resources.ModerationReportRequest
import dev.alllexey.itmowidgets.core.model.resources.ReportReason
import dev.alllexey.itmowidgets.core.model.resources.ResourceVoteRequest
import dev.alllexey.itmowidgets.core.model.reviews.SaveTeacherReviewRequest
import dev.alllexey.itmowidgets.core.model.reviews.TeacherReviewsResponse
import java.util.concurrent.TimeUnit
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class TeacherReviewApiTest {
    private val fixtures = TeacherReviewContractFixtures

    private data class Call(val method: String, val path: String, val body: String?, val reply: TeacherReviewsResponse,
        val run: suspend () -> ApiResponse<TeacherReviewsResponse>)

    @Test
    fun `every review route has an exact verb path body and typed reply`() = MockWebServer().use { server ->
        val api = client(server).api
        val id = fixtures.namedId
        val withoutMine = fixtures.response.copy(mine = null)
        val implicit = SaveTeacherReviewRequest(text = fixtures.TEXT)
        assertCalls(server, listOf(
            Call("GET", "/api/teachers/100001/reviews", null, fixtures.response) { api.teacherReviews(100001) },
            Call("PUT", "/api/teachers/100001/reviews/mine",
                """{"subjectTitle":"Математика","text":"${fixtures.TEXT}","anonymous":false,"flowIds":[93724,93725]}""",
                fixtures.response) { api.saveMyTeacherReview(100001, fixtures.save) },
            Call("PUT", "/api/teachers/100001/reviews/mine", """{"text":"${fixtures.TEXT}","anonymous":true,"flowIds":[]}""",
                fixtures.response) { api.saveMyTeacherReview(100001, implicit) },
            Call("DELETE", "/api/teachers/100001/reviews/mine", null, withoutMine) { api.deleteMyTeacherReview(100001) },
            Call("PUT", "/api/reviews/$id/vote", """{"value":-1}""", fixtures.response) {
                api.voteTeacherReview(id, ResourceVoteRequest(-1))
            },
            Call("POST", "/api/reviews/$id/report", """{"reason":"WRONG_TEACHER","comment":"Вёл другой"}""", fixtures.response) {
                api.reportTeacherReview(id, ModerationReportRequest(ReportReason.WRONG_TEACHER, "Вёл другой"))
            },
            Call("POST", "/api/reviews/${fixtures.copyId}/report", """{"reason":"OFFENSIVE"}""", fixtures.response) {
                api.reportTeacherReview(fixtures.copyId, ModerationReportRequest(ReportReason.OFFENSIVE))
            },
        ))
    }

    @Test
    fun `a teacher without reviews returns an empty typed response`() = MockWebServer().use { server ->
        server.enqueue(response("""{"teacherIsu":100002,"providerUrl":"https://onetwozzzplus.github.io/reviews/#/teacher/100002",
            "reviews":[],"mine":null,"canWrite":true,"canVote":true,"canReport":true,"knownTeacher":false}"""))

        val result = runBlocking { client(server).api.teacherReviews(100002) }

        assertTrue(result.success)
        assertEquals(TeacherReviewsResponse(100002, "https://onetwozzzplus.github.io/reviews/#/teacher/100002", emptyList(), null,
            canWrite = true, canVote = true, canReport = true, knownTeacher = false), result.data)
        assertEquals("/api/teachers/100002/reviews", server.takeRequest(5, TimeUnit.SECONDS)?.path)
    }

    @Test
    fun `a response in the replaced external shape is rejected`(): Unit = MockWebServer().use { server ->
        server.enqueue(response("""{"teacherIsu":100001,"providerUrl":"${fixtures.PROVIDER_URL}","external":[]}"""))

        assertFailsWith<JsonParseException> { runBlocking { client(server).api.teacherReviews(100001) } }
    }

    private fun assertCalls(server: MockWebServer, calls: List<Call>) {
        val gson = client(server).gson
        for (call in calls) {
            server.enqueue(response(gson.toJson(call.reply)))
            val result = runBlocking { call.run() }
            assertTrue(result.success, call.path)
            assertEquals(call.reply, result.data, call.path)
            assertNull(result.error)
            val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals(call.method, request.method)
            assertEquals(call.path, request.path)
            if (call.body == null) assertEquals(0, request.bodySize, call.path)
            else assertEquals(JsonParser.parseString(call.body), JsonParser.parseString(request.body.readUtf8()), call.path)
            assertNull(request.getHeader("Authorization"))
        }
    }

    private fun client(server: MockWebServer) = object : ItmoWidgetsImpl(MyItmo(), server.url("/").toString()) {
        override fun getValidToken(): String? = null
    }

    private fun response(data: String) = MockResponse().setHeader("Content-Type", "application/json")
        .setBody("""{"success":true,"data":$data,"error":null}""")
}
