package reviews

import api.myitmo.MyItmo
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.reviews.ExternalTeacherReview
import dev.alllexey.itmowidgets.core.model.reviews.TeacherReviewsResponse
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class TeacherReviewApiTest {
    @Test
    fun `teacher reviews uses an exact GET path empty body and typed response`() = MockWebServer().use { server ->
        val id = UUID.fromString("00000000-0000-0000-0000-000000000077")
        val providerUrl = "https://onetwozzzplus.github.io/reviews/#/teacher/100001"
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""
            {"success":true,"data":{"teacherIsu":100001,"providerUrl":"$providerUrl","external":[
            {"id":"$id","subjectTitle":"Тестовый предмет","writtenOn":"2025-01-25","writtenBeforeYear":null,
            "sourceTitle":null,"sourceLink":null,"text":"Тестовый отзыв"}]},"error":null}
        """.trimIndent()))

        val result = runBlocking { client(server).api.teacherReviews(100001) }

        assertTrue(result.success)
        assertEquals(TeacherReviewsResponse(100001, providerUrl, listOf(ExternalTeacherReview(
            id, "Тестовый предмет", LocalDate.of(2025, 1, 25), null, null, null, "Тестовый отзыв",
        ))), result.data)
        assertNull(result.error)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/teachers/100001/reviews", request.path)
        assertEquals(0, request.bodySize)
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun `a teacher without external reviews returns an empty typed list`() = MockWebServer().use { server ->
        val providerUrl = "https://onetwozzzplus.github.io/reviews/#/teacher/100002"
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""
            {"success":true,"data":{"teacherIsu":100002,"providerUrl":"$providerUrl","external":[]},"error":null}
        """.trimIndent()))

        val result = runBlocking { client(server).api.teacherReviews(100002) }

        assertTrue(result.success)
        assertEquals(TeacherReviewsResponse(100002, providerUrl, emptyList()), result.data)
        assertEquals("/api/teachers/100002/reviews", server.takeRequest(5, TimeUnit.SECONDS)?.path)
    }

    private fun client(server: MockWebServer) = object : ItmoWidgetsImpl(MyItmo(), server.url("/").toString()) {
        override fun getValidToken(): String? = null
    }
}
