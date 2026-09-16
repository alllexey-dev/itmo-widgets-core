package social

import api.myitmo.MyItmo
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.ItmoWidgetsApi
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.ApiResponse
import dev.alllexey.itmowidgets.core.model.UserCapabilities
import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.social.*
import java.util.concurrent.TimeUnit
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.http.*

class SocialApiContractTest {
    private val gson = ItmoWidgetsImpl(MyItmo()).gson
    private val identity = UserData(123456, "Synthetic user", null, emptyList(), UserCapabilities(false, true))

    @Test
    fun `all relationship states and capability combinations round trip with exact wire fields`() {
        for (relationship in RelationshipState.entries) {
            for (schedule in listOf(false, true)) {
                for (sport in listOf(false, true)) {
                    val profile = UserProfile(identity.copy(capabilities = UserCapabilities(schedule, sport)), relationship)
                    val encoded = gson.toJsonTree(profile).asJsonObject
                    assertEquals(setOf("user", "relationship"), encoded.keySet())
                    assertEquals(relationship.name, encoded["relationship"].asString)
                    assertEquals(setOf("isu", "name", "groups", "capabilities"), encoded["user"].asJsonObject.keySet())
                    assertEquals(profile, gson.fromJson(encoded, UserProfile::class.java))
                }
            }
            assertEquals(relationship, gson.fromJson(gson.toJson(relationship), RelationshipState::class.java))
        }
        val request = UserLookupRequest(listOf(123456, 456789))
        assertEquals(JsonParser.parseString("""{"isus":[123456,456789]}"""), gson.toJsonTree(request))
        assertEquals(request, gson.fromJson(gson.toJson(request), UserLookupRequest::class.java))
        val response = UserLookupResponse(listOf(UserProfile(identity, RelationshipState.FRIENDS)))
        assertEquals(response, gson.fromJson(gson.toJson(response), UserLookupResponse::class.java))
    }

    @Test
    fun `unknown null and malformed relationships are rejected instead of becoming NONE`() {
        for (wire in listOf("null", "42", "true", "{}", "[]", "\"friends\"", "\"UNKNOWN\"")) {
            assertFailsWith<JsonParseException> { gson.fromJson(wire, RelationshipState::class.java) }
            assertFailsWith<JsonParseException> {
                gson.fromJson("""{"user":${gson.toJson(identity)},"relationship":$wire}""", UserProfile::class.java)
            }
        }
        val missing = listOf(
            "{}", """{"user":${gson.toJson(identity)}}""", """{"relationship":"NONE"}""",
            """{"user":null,"relationship":"NONE"}""",
            """{"user":${gson.toJson(identity)},"relationship":"NONE","relationship":"FRIENDS"}""",
            """{"user":{"isu":123456,"name":"Synthetic user","groups":[]},"relationship":"FRIENDS"}""",
        )
        for (wire in missing) {
            assertFailsWith<JsonParseException> { gson.fromJson(wire, UserProfile::class.java) }
        }
    }

    @Test
    fun `every relationship action uses the target ISU path no body and reads a fresh profile`() = withServer { server, api ->
        val actions: List<Triple<String, String, suspend (Int) -> ApiResponse<UserProfile>>> = listOf(
            Triple("POST", "/request", api::sendFriendRequest),
            Triple("POST", "/accept", api::acceptFriendRequest),
            Triple("POST", "/reject", api::rejectFriendRequest),
            Triple("POST", "/cancel", api::cancelFriendRequest),
            Triple("DELETE", "", api::removeFriend),
        )
        for ((method, suffix, call) in actions) {
            server.enqueue(response(profileJson("INCOMING")))
            val result = runBlocking { call(123456) }
            assertEquals(UserProfile(identity, RelationshipState.INCOMING), result.data)
            val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals(method, request.method)
            assertEquals("/api/friends/123456$suffix", request.path)
            assertEquals(0L, request.bodySize)
            assertNull(request.requestUrl?.query)
            assertNull(request.getHeader("Authorization"))
        }
    }

    @Test
    fun `every friendship list uses the new profile wrapper and exact GET route`() = withServer { server, api ->
        val reads: List<Pair<String, suspend () -> ApiResponse<List<UserProfile>>>> = listOf(
            "" to api::friends, "/requests/incoming" to api::incomingFriendRequests,
            "/requests/outgoing" to api::outgoingFriendRequests,
        )
        for ((suffix, call) in reads) {
            server.enqueue(response("[${profileJson("FRIENDS")}]"))
            assertEquals(listOf(UserProfile(identity, RelationshipState.FRIENDS)), runBlocking { call() }.data)
            val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("GET", request.method)
            assertEquals("/api/friends$suffix", request.path)
            assertEquals(0L, request.bodySize)
            assertNull(request.requestUrl?.query)
        }
    }

    @Test
    fun `public profile uses an ISU path and does not reuse own data route`() = withServer { server, api ->
        server.enqueue(response(profileJson("OUTGOING")))
        assertEquals(UserProfile(identity, RelationshipState.OUTGOING), runBlocking { api.userProfile(123456) }.data)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/users/123456", request.path)
        assertEquals(0L, request.bodySize)
        assertNull(request.requestUrl?.query)
    }

    @Test
    fun `lookup sends exact ISU list as JSON and returns registered profiles only`() = withServer { server, api ->
        server.enqueue(response("""{"users":[${profileJson("NONE")}]}"""))
        val result = runBlocking { api.lookupUsers(UserLookupRequest(listOf(123456, 999999, 123456))) }
        assertEquals(UserLookupResponse(listOf(UserProfile(identity, RelationshipState.NONE))), result.data)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("POST", request.method)
        assertEquals("/api/users/lookup", request.path)
        assertEquals(JsonParser.parseString("""{"isus":[123456,999999,123456]}"""), JsonParser.parseString(request.body.readUtf8()))
        assertNull(request.requestUrl?.query)
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun `empty lookup remains typed`() = withServer { server, api ->
        server.enqueue(response("""{"users":[]}"""))
        assertEquals(emptyList(), runBlocking { api.lookupUsers(UserLookupRequest(emptyList())) }.data?.users)
        assertEquals("""{"isus":[]}""", assertNotNull(server.takeRequest(5, TimeUnit.SECONDS)).body.readUtf8())
    }

    @Test
    fun `incomplete profile fails through Retrofit while an error may contain null data`() = withServer { server, api ->
        server.enqueue(response("""{"user":${gson.toJson(identity)}}"""))
        assertFailsWith<JsonParseException> { runBlocking { api.userProfile(123456) } }
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json")
            .setBody("""{"success":false,"data":null,"error":{"code":"not_found","message":"User not found"}}"""))
        val error = runBlocking { api.userProfile(123456) }
        assertFalse(error.success)
        assertNull(error.data)
        assertEquals("not_found", error.error?.code)
    }

    @Test
    fun `obsolete friend methods and routes are absent`() {
        val methods = ItmoWidgetsApi::class.java.methods
        assertFalse(methods.any { it.name == "addFriend" || it.name == "myFriends" })
        val routes = methods.flatMap {
            listOfNotNull(it.getAnnotation(GET::class.java)?.value, it.getAnnotation(POST::class.java)?.value,
                it.getAnnotation(DELETE::class.java)?.value)
        }
        for (route in listOf("/api/friends/add", "/api/friends/remove", "/api/friends/get")) assertFalse(route in routes)
    }

    @Test
    fun `target friends use exact ISU path and retain viewer relative relationships`() = withServer { server, api ->
        server.enqueue(response("[${profileJson("NONE")}]"))
        assertEquals(listOf(UserProfile(identity, RelationshipState.NONE)), runBlocking { api.userFriends(456789) }.data)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/users/456789/friends", request.path)
        assertEquals(0L, request.bodySize)
        assertNull(request.requestUrl?.query)
    }

    private fun profileJson(state: String) =
        """{"user":{"isu":123456,"name":"Synthetic user","pictureUrl":null,"groups":[],"capabilities":{"canViewSchedule":false,"canViewSport":true,"canViewFriends":false}},"relationship":"$state"}"""

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
