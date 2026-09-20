package schedule

import api.myitmo.MyItmo
import dev.alllexey.itmowidgets.core.ItmoWidgetsApi
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.UserCapabilities
import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.social.RelationshipState
import dev.alllexey.itmowidgets.core.model.social.UserProfile
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class LessonContextApiTest {
    private val gson = ItmoWidgetsImpl(MyItmo()).gson
    private val friend = UserProfile(
        UserData(123456, "Synthetic friend", null, emptyList(), UserCapabilities(true, false)),
        RelationshipState.FRIENDS,
    )

    @Test
    fun `friends on a lesson resolve the pair path and send the occurrence date as the only query`() = withServer { server, api ->
        server.enqueue(response(gson.toJson(listOf(friend))))

        val friends = runBlocking { api.friendsOnLesson(2_147_483_648L, LocalDate.of(2026, 9, 8)) }.data

        assertEquals(listOf(friend), friends)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/schedule/lessons/2147483648/friends?date=2026-09-08", request.path)
        assertEquals("date=2026-09-08", request.requestUrl?.query)
        assertEquals(0L, request.bodySize)
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun `an empty friend list is a plain empty list`() = withServer { server, api ->
        server.enqueue(response("[]"))

        assertEquals(emptyList(), runBlocking { api.friendsOnLesson(42, LocalDate.of(2026, 9, 8)) }.data)
    }

    @Test
    fun `the unrestricted participant call is gone from the contract`() {
        assertFalse(ItmoWidgetsApi::class.java.methods.any { it.name == "usersByPairId" })
    }

    private fun response(data: String) = MockResponse().setHeader("Content-Type", "application/json")
        .setBody("""{"success":true,"data":$data,"error":null}""")

    private fun withServer(block: (MockWebServer, ItmoWidgetsApi) -> Unit) {
        MockWebServer().use { server ->
            val client = object : ItmoWidgetsImpl(MyItmo(), server.url("/").toString()) {
                override fun getValidToken(): String? = null
            }
            block(server, client.api)
        }
    }
}
