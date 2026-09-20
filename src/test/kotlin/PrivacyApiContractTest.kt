import api.myitmo.MyItmo
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.model.social.UserProfile
import dev.alllexey.itmowidgets.core.model.social.RelationshipState
import dev.alllexey.itmowidgets.core.ItmoWidgetsApi
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.GroupData
import dev.alllexey.itmowidgets.core.model.SharingVisibility
import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.UserPrivacySettings
import dev.alllexey.itmowidgets.core.model.UserCapabilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit
import retrofit2.http.GET
import retrofit2.http.PUT

class PrivacyApiContractTest {

    private val gson = ItmoWidgetsImpl(MyItmo()).gson

    @Test
    fun `all nine privacy audience combinations round trip with exact wire names`() {
        for (schedule in SharingVisibility.entries) {
            for (sport in SharingVisibility.entries) {
                val settings = UserPrivacySettings(scheduleVisibility = schedule, sportVisibility = sport)
                val json = gson.toJson(settings)

                assertEquals(
                    JsonParser.parseString("""{"scheduleVisibility":"${schedule.name}","sportVisibility":"${sport.name}","friendsVisibility":"ALL"}"""),
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
            "sportVisibility":"NOBODY","friendsVisibility":"ALL",
            "futureField":{"nested":[true,42,"new"]}
        }"""

        assertEquals(
            UserPrivacySettings(SharingVisibility.ALL, SharingVisibility.NOBODY),
            gson.fromJson(json, UserPrivacySettings::class.java)
        )
    }

    @Test
    fun `viewer capabilities and user data round trip without owner privacy settings`() {
        val capabilities = UserCapabilities(canViewSchedule = true, canViewSport = false)
        val capabilitiesJson = """{"canViewSchedule":true,"canViewSport":false,"canViewFriends":false}"""

        assertEquals(JsonParser.parseString(capabilitiesJson), JsonParser.parseString(gson.toJson(capabilities)))
        assertEquals(capabilities, gson.fromJson(capabilitiesJson, UserCapabilities::class.java))

        val user = UserData(
            isu = 123456,
            name = "Тестовый пользователь",
            pictureUrl = null,
            groups = emptyList(),
            capabilities = capabilities
        )
        val encoded = gson.toJsonTree(user).asJsonObject
        assertEquals(setOf("isu", "name", "groups", "capabilities"), encoded.keySet())
        assertEquals(JsonParser.parseString(capabilitiesJson), encoded.get("capabilities"))
        assertEquals(user, gson.fromJson(gson.toJson(user), UserData::class.java))
    }

    @Test
    fun `privacy GET uses the dedicated audience route`() = withServer { server, api ->
        server.enqueue(response("""{"scheduleVisibility":"FRIENDS","sportVisibility":"ALL","friendsVisibility":"ALL"}"""))

        val privacy = runBlocking { api.myPrivacySettings() }

        assertEquals(UserPrivacySettings(SharingVisibility.FRIENDS, SharingVisibility.ALL), privacy.data)
        val privacyRequest = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", privacyRequest.method)
        assertEquals("/api/users/me/privacy", privacyRequest.path)
        assertEquals(0L, privacyRequest.bodySize)
        assertNull(privacyRequest.getHeader("Authorization"))
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
            JsonParser.parseString("""{"scheduleVisibility":"ALL","sportVisibility":"NOBODY","friendsVisibility":"ALL"}"""),
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
    fun `all four viewer capability combinations round trip with exact booleans and no owner settings`() {
        for (schedule in listOf(false, true)) {
            for (sport in listOf(false, true)) {
                val capabilities = UserCapabilities(schedule, sport)
                val expectedJson = """{"canViewSchedule":$schedule,"canViewSport":$sport,"canViewFriends":false}"""
                assertEquals(JsonParser.parseString(expectedJson), gson.toJsonTree(capabilities))
                assertEquals(capabilities, gson.fromJson(expectedJson, UserCapabilities::class.java))

                val profile = user(capabilities)
                val encoded = gson.toJsonTree(profile).asJsonObject
                assertEquals(setOf("isu", "name", "pictureUrl", "groups", "capabilities"), encoded.keySet())
                assertEquals(JsonParser.parseString(expectedJson), encoded.get("capabilities"))
                assertFalse(encoded.has("settings"))
                assertFalse(encoded.has("scheduleVisibility"))
                assertFalse(encoded.has("sportVisibility"))
                assertEquals(profile, gson.fromJson(gson.toJson(profile), UserData::class.java))
            }
        }
    }

    @Test
    fun `null and non boolean permissions are rejected for each capability field standalone and in profiles`() {
        for (field in listOf("canViewSchedule", "canViewSport")) {
            for (invalid in listOf("null", "0", "1", "\"true\"", "\"false\"", "\"TRUE\"", "{}", "[]")) {
                val other = if (field == "canViewSchedule") "canViewSport" else "canViewSchedule"
                val json = """{"$field":$invalid,"$other":false}"""
                assertFailsWith<JsonParseException> { gson.fromJson(json, UserCapabilities::class.java) }
                assertFailsWith<JsonParseException> { gson.fromJson(profileJson(json), UserData::class.java) }
            }
        }
    }

    @Test
    fun `missing permission fields are rejected and never supplied as permissive defaults`() {
        for (json in listOf("{}", """{"canViewSchedule":false}""", """{"canViewSport":true}""")) {
            assertFailsWith<JsonParseException> { gson.fromJson(json, UserCapabilities::class.java) }
            assertFailsWith<JsonParseException> { gson.fromJson(profileJson(json), UserData::class.java) }
        }
    }

    @Test
    fun `capabilities must be an object rather than null or a scalar`() {
        for (json in listOf("null", "true", "false", "0", "[]", "\"capabilities\"")) {
            assertFailsWith<JsonParseException> { gson.fromJson(json, UserCapabilities::class.java) }
            assertFailsWith<JsonParseException> { gson.fromJson(profileJson(json), UserData::class.java) }
        }
    }

    @Test
    fun `duplicate permission fields are rejected including when the first value is false`() {
        for (field in listOf("canViewSchedule", "canViewSport")) {
            val other = if (field == "canViewSchedule") "canViewSport" else "canViewSchedule"
            val json = """{"$field":false,"$other":false,"$field":true}"""
            assertFailsWith<JsonParseException> { gson.fromJson(json, UserCapabilities::class.java) }
            assertFailsWith<JsonParseException> { gson.fromJson(profileJson(json), UserData::class.java) }
        }
    }

    @Test
    fun `unknown response metadata is ignored without becoming a permission or retained owner setting`() {
        val capabilitiesJson = """{
            "canViewSchedule":false,"canViewSport":true,"canViewFriends":false,
            "futureField":{"nested":[true,42,"new"]},
            "sportVisibility":"ALL","scheduleVisibility":"ALL"
        }"""
        val expected = UserCapabilities(false, true)
        assertEquals(expected, gson.fromJson(capabilitiesJson, UserCapabilities::class.java))
        val profile = gson.fromJson(profileJson(capabilitiesJson), UserData::class.java)
        assertEquals(expected, profile.capabilities)
        assertEquals(
            JsonParser.parseString("""{"canViewSchedule":false,"canViewSport":true,"canViewFriends":false}"""),
            gson.toJsonTree(profile).asJsonObject.get("capabilities"),
        )
    }

    @Test
    fun `missing entire capabilities including legacy settings only profile fails closed`() {
        val noCapabilities = JsonParser.parseString(profileJson(VALID_CAPABILITIES)).asJsonObject.apply {
            remove("capabilities")
        }
        val oldProfile = noCapabilities.deepCopy().apply {
            add("settings", JsonParser.parseString("""{"sportSharing":true,"scheduleSharing":true}"""))
        }
        for (json in listOf(noCapabilities.toString(), oldProfile.toString())) {
            val failure = assertFailsWith<JsonParseException> { gson.fromJson(json, UserData::class.java) }
            assertFalse(failure.message.orEmpty().contains("Synthetic user"))
            assertFalse(failure.message.orEmpty().contains("sportSharing"))
        }
    }

    @Test
    fun `Core API has no legacy privacy route or methods while both version endpoints remain`() {
        val methods = ItmoWidgetsApi::class.java.methods
        val routes = methods.flatMap { method ->
            listOfNotNull(method.getAnnotation(GET::class.java)?.value, method.getAnnotation(PUT::class.java)?.value)
        }
        assertFalse(methods.any { it.name == "mySettings" || it.name == "updateMySettings" })
        assertFalse(routes.contains("/api/users/me/settings"))
        assertEquals(2, routes.count { it == "/api/users/me/privacy" })
        assertTrue(routes.contains("/api/app/version"))
        assertTrue(routes.contains("/api/app/version-info"))
    }

    @Test
    fun `own profile GET consumes capabilities from the unchanged profile route`() = withServer { server, api ->
        val expected = user(UserCapabilities(true, true))
        server.enqueue(response(gson.toJson(expected)))

        val result = runBlocking { api.myUserData() }

        assertEquals(expected, result.data)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/users/me/data", request.path)
        assertEquals(0L, request.bodySize)
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun `friends route consumes every viewer permission combination without interpreting owner audiences`() = withServer { server, api ->
        val profiles = listOf(false, true).flatMap { schedule ->
            listOf(false, true).map { sport -> UserProfile(user(UserCapabilities(schedule, sport)), RelationshipState.FRIENDS) }
        }
        server.enqueue(response(gson.toJson(profiles)))

        val result = runBlocking { api.friends() }

        assertEquals(profiles, result.data)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/friends", request.path)
        assertNull(request.requestUrl?.query)
    }

    @Test
    fun `old profile wire shape is rejected through the real Retrofit converter`() = withServer { server, api ->
        val oldProfile = JsonParser.parseString(profileJson(VALID_CAPABILITIES)).asJsonObject.apply {
            remove("capabilities")
            add("settings", JsonParser.parseString("""{"sportSharing":true,"scheduleSharing":true}"""))
        }
        server.enqueue(response(oldProfile.toString()))

        assertFailsWith<JsonParseException> { runBlocking { api.myUserData() } }

        assertEquals("/api/users/me/data", assertNotNull(server.takeRequest(5, TimeUnit.SECONDS)).path)
    }

    @Test
    fun `failed profile response may still contain null data without creating a null capabilities user`() = withServer { server, api ->
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(
            """{"success":false,"data":null,"error":{"message":"Access denied","code":"permission_denied"}}""",
        ))

        val result = runBlocking { api.myUserData() }

        assertFalse(result.success)
        assertNull(result.data)
        assertEquals("permission_denied", result.error?.code)
    }

    @Test
    fun `friends audience and capability round trip for every value`() {
        for (schedule in SharingVisibility.entries) for (sport in SharingVisibility.entries) {
            for (friends in SharingVisibility.entries) {
                val settings = UserPrivacySettings(schedule, sport, friends)
                val encoded = gson.toJsonTree(settings).asJsonObject
                assertEquals(setOf("scheduleVisibility", "sportVisibility", "friendsVisibility"), encoded.keySet())
                assertEquals(friends.name, encoded["friendsVisibility"].asString)
                assertEquals(settings, gson.fromJson(encoded, UserPrivacySettings::class.java))
            }
        }
        for (schedule in listOf(false, true)) for (sport in listOf(false, true)) for (friends in listOf(false, true)) {
            val capabilities = UserCapabilities(schedule, sport, friends)
            assertEquals(capabilities, gson.fromJson(gson.toJson(capabilities), UserCapabilities::class.java))
        }
    }

    @Test
    fun `new friends fields fail closed when missing null malformed or duplicated`() {
        for (field in listOf("", ",\"friendsVisibility\":null", ",\"friendsVisibility\":\"UNKNOWN\"",
            ",\"friendsVisibility\":true", ",\"friendsVisibility\":\"ALL\",\"friendsVisibility\":\"NOBODY\"")) {
            assertFailsWith<JsonParseException> {
                gson.fromJson("""{"scheduleVisibility":"ALL","sportVisibility":"ALL"$field}""", UserPrivacySettings::class.java)
            }
        }
        for (field in listOf("", ",\"canViewFriends\":null", ",\"canViewFriends\":\"true\"",
            ",\"canViewFriends\":1", ",\"canViewFriends\":false,\"canViewFriends\":true")) {
            assertFailsWith<JsonParseException> {
                gson.fromJson("""{"canViewSchedule":true,"canViewSport":true$field}""", UserCapabilities::class.java)
            }
        }
    }

    private fun user(capabilities: UserCapabilities) = UserData(
        isu = 123456,
        name = "Synthetic user",
        pictureUrl = "https://example.invalid/synthetic-avatar",
        groups = listOf(GroupData("M3100", 1, "SYN")),
        capabilities = capabilities,
    )

    private fun profileJson(capabilitiesJson: String) = """{
        "isu":123456,"name":"Synthetic user","pictureUrl":null,"groups":[],
        "capabilities":$capabilitiesJson
    }"""

    private companion object {
        const val VALID_CAPABILITIES = """{"canViewSchedule":false,"canViewSport":true,"canViewFriends":false}"""
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
