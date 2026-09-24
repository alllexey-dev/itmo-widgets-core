package weblogin

import api.myitmo.MyItmo
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.ApiResponse
import dev.alllexey.itmowidgets.core.model.WebLoginPreview
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.HttpException

class WebLoginApiTest {
    private val challengeId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000077")
    private val preview = WebLoginPreview(challengeId, "Mozilla/5.0 (X11; Linux x86_64)",
        OffsetDateTime.parse("2026-09-24T10:00:00Z"), OffsetDateTime.parse("2026-09-24T10:05:00Z"))

    private data class Call(val method: String, val path: String, val data: String, val expected: Any, val run: suspend () -> ApiResponse<*>)

    @Test
    fun `web login and role routes have an exact verb path empty body and typed reply`() = MockWebServer().use { server ->
        val api = client(server).api
        val calls = listOf(
            Call("GET", "/api/users/me/roles", """["MODERATOR","ADMIN"]""", listOf("MODERATOR", "ADMIN")) { api.myRoles() },
            Call("GET", "/api/users/me/web-login/K7M2QX9P", """{"challengeId":"$challengeId",
                "userAgent":"Mozilla/5.0 (X11; Linux x86_64)","createdAt":"2026-09-24T10:00:00Z",
                "expiresAt":"2026-09-24T10:05:00Z"}""", preview) { api.webLoginPreview("K7M2QX9P") },
            Call("POST", "/api/users/me/web-login/$challengeId/approve", "{}", Unit) { api.approveWebLogin(challengeId) },
        )
        for (call in calls) {
            server.enqueue(response(call.data))
            val result = runBlocking { call.run() }
            assertTrue(result.success, call.path)
            if (call.expected == Unit) assertNotNull(result.data, call.path) else assertEquals(call.expected, result.data, call.path)
            val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals(call.method, request.method, call.path)
            assertEquals(call.path, request.path)
            assertEquals(0, request.bodySize, call.path)
            assertNull(request.getHeader("Authorization"))
        }
    }

    @Test
    fun `unknown future roles stay plain strings`() = MockWebServer().use { server ->
        server.enqueue(response("""["ADMIN","SUPPORT_AGENT"]"""))
        assertEquals(listOf("ADMIN", "SUPPORT_AGENT"), runBlocking { client(server).api.myRoles() }.data)
    }

    @Test
    fun `an expired or unknown code surfaces as HTTP 404`() = MockWebServer().use { server ->
        server.enqueue(MockResponse().setResponseCode(404).setHeader("Content-Type", "application/json")
            .setBody("""{"success":false,"data":null,"error":{"code":"not_found","message":"Not found"}}"""))
        val error = assertFailsWith<HttpException> { runBlocking { client(server).api.webLoginPreview("ZZZZZZZZ") } }
        assertEquals(404, error.code())
        assertEquals("/api/users/me/web-login/ZZZZZZZZ", server.takeRequest(5, TimeUnit.SECONDS)?.path)
    }

    private fun client(server: MockWebServer) = object : ItmoWidgetsImpl(MyItmo(), server.url("/").toString()) {
        override fun getValidToken(): String? = null
    }

    private fun response(data: String) = MockResponse().setHeader("Content-Type", "application/json")
        .setBody("""{"success":true,"data":$data,"error":null}""")
}
