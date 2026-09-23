package resources

import api.myitmo.MyItmo
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.*
import dev.alllexey.itmowidgets.core.model.ApiResponse
import dev.alllexey.itmowidgets.core.model.resources.*
import java.util.concurrent.TimeUnit
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class SubjectLinkApiTest {
    private val fixtures = ResourceContractFixtures

    private data class Call(val method: String, val path: String, val body: Any?, val reply: Any, val run: suspend () -> ApiResponse<*>)

    @Test
    fun `every link route has an exact verb path query body and typed reply`() = MockWebServer().use { server ->
        val client = client(server)
        val api = client.api
        val id = fixtures.id
        val pin = PinSubjectLinkRequest("2026-1", id)
        val unpin = PinSubjectLinkRequest("2026-1")
        val report = ModerationReportRequest(ReportReason.BROKEN, "Не открывается")
        assertCalls(server, client, listOf(
            Call("GET", "/api/subjects/42/links?period=2026-1", null, fixtures.links) { api.subjectLinks(42, "2026-1") },
            Call("PUT", "/api/links/$id", fixtures.save, fixtures.link) { api.saveSubjectLink(id, fixtures.save) },
            Call("DELETE", "/api/links/$id", null, Unit) { api.deleteSubjectLink(id) },
            Call("PUT", "/api/links/$id/saved", SetLinkSavedRequest(true), fixtures.link) { api.setSubjectLinkSaved(id, SetLinkSavedRequest(true)) },
            Call("PUT", "/api/subjects/42/links/pin", pin, fixtures.links) { api.pinSubjectLink(42, pin) },
            Call("PUT", "/api/subjects/42/links/pin", unpin, fixtures.links.copy(pinnedId = null)) { api.pinSubjectLink(42, unpin) },
            Call("PUT", "/api/links/$id/vote", ResourceVoteRequest(-1), fixtures.link) { api.voteSubjectLink(id, ResourceVoteRequest(-1)) },
            Call("POST", "/api/links/$id/report", report, fixtures.link) { api.reportSubjectLink(id, report) },
            Call("GET", "/api/users/me/restrictions", null, listOf(fixtures.restriction)) { api.myRestrictions() },
        ))
        val removed = setOf("myResources", "subjectResources", "savePersonalResource", "selectSubjectResource", "submitPersonalResource",
            "withdrawResourceSubmission", "resourceSubmissions", "voteResource", "reportResource", "deletePersonalResource")
        assertTrue(ItmoWidgetsApi::class.java.methods.none { it.name in removed })
    }

    @Test
    fun `moderator routes are separate exact and decode the link case target`() = MockWebServer().use { server ->
        val client = client(server)
        val api = client.moderationApi
        val id = fixtures.id
        val decision = ModerationDecisionRequest(ModerationAction.APPROVE, "Проверено")
        assertCalls(server, client, listOf(
            Call("GET", "/api/moderation/cases?status=OPEN", null, listOf(fixtures.case)) { api.moderationCases("OPEN") },
            Call("POST", "/api/moderation/cases/$id/decisions", decision, fixtures.case) { api.decide(id, decision) },
            Call("GET", "/api/moderation/restrictions?isu=123456", null, listOf(fixtures.restriction)) { api.userRestrictions(123456) },
            Call("POST", "/api/moderation/restrictions/$id/revoke", null, Unit) { api.revokeRestriction(id) },
            Call("GET", "/api/moderation/settings", null, fixtures.settings) { api.moderationSettings() },
            Call("PUT", "/api/moderation/settings", fixtures.settings, fixtures.settings) { api.updateModerationSettings(fixtures.settings) },
        ))
        assertTrue(ItmoWidgetsApi::class.java.methods.none { it.name in setOf("moderationCases", "decide", "userRestrictions", "revokeRestriction", "moderationSettings", "updateModerationSettings") })
    }

    private fun assertCalls(server: MockWebServer, client: ItmoWidgetsImpl, calls: List<Call>) {
        val gson = client.gson
        for (call in calls) {
            server.enqueue(response(gson.toJson(call.reply)))
            val result = runBlocking { call.run() }
            assertTrue(result.success, call.path)
            if (call.reply != Unit) assertEquals(call.reply, result.data, call.path)
            val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals(call.method, request.method)
            assertEquals(call.path, request.path)
            if (call.body == null) assertEquals(0, request.bodySize)
            else assertEquals(gson.toJsonTree(call.body), JsonParser.parseString(request.body.readUtf8()), call.path)
            assertNull(request.getHeader("Authorization"))
        }
    }

    private fun client(server: MockWebServer) = object : ItmoWidgetsImpl(MyItmo(), server.url("/").toString()) {
        override fun getValidToken(): String? = null
    }

    private fun response(data: String) = MockResponse().setHeader("Content-Type", "application/json")
        .setBody("""{"success":true,"data":$data,"error":null}""")
}
