import api.myitmo.MyItmo
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.ItmoWidgetsApi
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.SharingVisibility
import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.UserPrivacySettings
import dev.alllexey.itmowidgets.core.model.UserSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit

class PrivacyApiContractTest {

    private val gson = ItmoWidgetsImpl(MyItmo()).gson

    @Test
    fun `all nine privacy audience combinations round trip with exact wire names`() {
        for (schedule in SharingVisibility.entries) {
            for (sport in SharingVisibility.entries) {
                val settings = UserPrivacySettings(scheduleVisibility = schedule, sportVisibility = sport)
                val json = gson.toJson(settings)

                assertEquals(
                    JsonParser.parseString("""{"scheduleVisibility":"${schedule.name}","sportVisibility":"${sport.name}"}"""),
                    JsonParser.parseString(json)
                )
                assertEquals(settings, gson.fromJson(json, UserPrivacySettings::class.java))
            }
        }
    }

    @Test
    fun `unknown null and non string visibility values fail closed for either privacy field`() {
        val invalid = listOf("\"UNKNOWN\"", "\"friends\"", "null", "42", "true", "{}", "[]")
        for (field in listOf("scheduleVisibility", "sportVisibility")) {
            for (value in invalid) {
                val other = if (field == "scheduleVisibility") "sportVisibility" else "scheduleVisibility"
                val json = """{"$field":$value,"$other":"FRIENDS"}"""

                assertFailsWith<JsonParseException>("Invalid $field must not become a permissive default: $value") {
                    gson.fromJson(json, UserPrivacySettings::class.java)
                }
            }
        }
    }

    @Test
    fun `absent privacy fields are rejected instead of fabricating friends defaults`() {
        for (json in listOf("{}", """{"scheduleVisibility":"FRIENDS"}""", """{"sportVisibility":"NOBODY"}""")) {
            assertFailsWith<JsonParseException>("Both privacy fields are required: $json") {
                gson.fromJson(json, UserPrivacySettings::class.java)
            }
        }
    }

    @Test
    fun `standalone visibility adapter accepts known values and rejects unsupported wire values`() {
        for (visibility in SharingVisibility.entries) {
            assertEquals(visibility, gson.fromJson("\"${visibility.name}\"", SharingVisibility::class.java))
        }
        for (json in listOf("\"PUBLIC\"", "\"all\"", "null", "0", "false", "{}", "[]")) {
            assertFailsWith<JsonParseException>("Invalid standalone visibility: $json") {
                gson.fromJson(json, SharingVisibility::class.java)
            }
        }
    }

    @Test
    fun `unknown additional response fields do not break valid privacy settings`() {
        val json = """{
            "scheduleVisibility":"ALL",
            "sportVisibility":"NOBODY",
            "futureField":{"nested":[true,42,"new"]}
        }"""

        assertEquals(
            UserPrivacySettings(SharingVisibility.ALL, SharingVisibility.NOBODY),
            gson.fromJson(json, UserPrivacySettings::class.java)
        )
    }

    @Test
    fun `legacy settings and user data retain boolean wire shape and round trip`() {
        val settings = UserSettings(sportSharing = false, scheduleSharing = true)
        val legacyJson = """{"sportSharing":false,"scheduleSharing":true}"""

        assertEquals(JsonParser.parseString(legacyJson), JsonParser.parseString(gson.toJson(settings)))
        assertEquals(settings, gson.fromJson(legacyJson, UserSettings::class.java))

        val user = UserData(
            isu = 123456,
            name = "Тестовый пользователь",
            pictureUrl = null,
            groups = emptyList(),
            settings = settings
        )
        val encoded = gson.toJsonTree(user).asJsonObject
        assertEquals(setOf("isu", "name", "groups", "settings"), encoded.keySet())
        assertEquals(JsonParser.parseString(legacyJson), encoded.get("settings"))
        assertEquals(user, gson.fromJson(gson.toJson(user), UserData::class.java))
    }

    @Test
    fun `privacy GET uses the dedicated route without changing legacy settings route`() = withServer { server, api ->
        server.enqueue(response("""{"scheduleVisibility":"FRIENDS","sportVisibility":"ALL"}"""))

        val privacy = runBlocking { api.myPrivacySettings() }

        assertEquals(UserPrivacySettings(SharingVisibility.FRIENDS, SharingVisibility.ALL), privacy.data)
        val privacyRequest = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", privacyRequest.method)
        assertEquals("/api/users/me/privacy", privacyRequest.path)
        assertEquals(0L, privacyRequest.bodySize)
        assertNull(privacyRequest.getHeader("Authorization"))

        server.enqueue(response("""{"sportSharing":false,"scheduleSharing":true}"""))
        assertEquals(UserSettings(false, true), runBlocking { api.mySettings() }.data)
        val legacyRequest = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", legacyRequest.method)
        assertEquals("/api/users/me/settings", legacyRequest.path)
    }

    @Test
    fun `privacy PUT sends both typed fields as a full replacement and reads the response`() = withServer { server, api ->
        val settings = UserPrivacySettings(SharingVisibility.ALL, SharingVisibility.NOBODY)
        server.enqueue(response(gson.toJson(settings)))

        val result = runBlocking { api.updateMyPrivacySettings(settings) }

        assertEquals(settings, result.data)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("PUT", request.method)
        assertEquals("/api/users/me/privacy", request.path)
        assertEquals(
            JsonParser.parseString("""{"scheduleVisibility":"ALL","sportVisibility":"NOBODY"}"""),
            JsonParser.parseString(request.body.readUtf8())
        )
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun `user sport bookings use ISU path and preserve confirmed lesson identifiers beyond Int range`() = withServer { server, api ->
        val identifiers = listOf(2_147_483_648L, 9_223_372_036_854_775_000L)
        server.enqueue(response("""{"lessonIds":[2147483648,9223372036854775000]}"""))

        val result = runBlocking { api.userSportBookings(123456) }

        assertEquals(identifiers, result.data?.lessonIds)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/sport/users/123456/bookings", request.path)
        assertNull(request.requestUrl?.query)
    }

    @Test
    fun `users by pair resolves the path placeholder without a duplicate query parameter`() = withServer { server, api ->
        server.enqueue(response("[]"))

        assertEquals(emptyList(), runBlocking { api.usersByPairId(2_147_483_648L) }.data)

        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/schedule/lessons/2147483648/users", request.path)
        assertNull(request.requestUrl?.query)
    }

    private fun response(data: String) = MockResponse()
        .setHeader("Content-Type", "application/json")
        .setBody("""{"success":true,"data":$data,"error":null}""")

    private fun withServer(block: (MockWebServer, ItmoWidgetsApi) -> Unit) {
        MockWebServer().use { server ->
            val client = object : ItmoWidgetsImpl(MyItmo(), server.url("/").toString()) {
                // These are wire/serialization tests; no ITMO session or token
                // provider is initialized, and no external auth request is needed.
                override fun getValidToken(): String? = null
            }
            block(server, client.api)
        }
    }
}
