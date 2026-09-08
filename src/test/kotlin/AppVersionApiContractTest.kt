import api.myitmo.MyItmo
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.ItmoWidgetsApi
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.AppVersionInfo
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class AppVersionApiContractTest {

    private val gson = ItmoWidgetsImpl(MyItmo()).gson

    @Test
    fun `version info round trips exact required fields with an empty note`() {
        val version = AppVersionInfo(minVersion = "2.1", latestVersion = "2.3", note = "")
        val json = gson.toJson(version)

        assertEquals(
            JsonParser.parseString("""{"minVersion":"2.1","latestVersion":"2.3","note":""}"""),
            JsonParser.parseString(json)
        )
        assertEquals(version, gson.fromJson(json, AppVersionInfo::class.java))
    }

    @Test
    fun `note preserves Unicode newlines and markup as literal text`() {
        val note = "Обновление ИТМО — расписание и спорт.\n<b>Это обычный текст</b> & не HTML.\n日本語 🙂"
        val version = AppVersionInfo(minVersion = "2.1", latestVersion = "2.1.1", note = note)

        val json = gson.toJson(version)
        val restored = gson.fromJson(json, AppVersionInfo::class.java)

        assertEquals(note, JsonParser.parseString(json).asJsonObject.get("note").asString)
        assertEquals(version, restored)
        assertEquals(note, restored.note)
    }

    @Test
    fun `each missing version info field is rejected without fabricated defaults`() {
        for (field in FIELDS) {
            val json = JsonParser.parseString(VALID_INFO).asJsonObject.apply { remove(field) }

            assertFailsWith<JsonParseException>("Missing required field: $field") {
                gson.fromJson(json, AppVersionInfo::class.java)
            }
        }
        assertFailsWith<JsonParseException> {
            gson.fromJson("{}", AppVersionInfo::class.java)
        }
    }

    @Test
    fun `each null or non string version info field is rejected`() {
        for (field in FIELDS) {
            for (value in listOf("null", "42", "true", "{}", "[]")) {
                val json = JsonParser.parseString(VALID_INFO).asJsonObject.apply {
                    add(field, JsonParser.parseString(value))
                }

                assertFailsWith<JsonParseException>("Invalid $field value: $value") {
                    gson.fromJson(json, AppVersionInfo::class.java)
                }
            }
        }
    }

    @Test
    fun `additional response fields are ignored while required strings stay unchanged`() {
        val json = JsonParser.parseString(VALID_INFO).asJsonObject.apply {
            add("futureMetadata", JsonParser.parseString("""{"values":[true,42,"new"]}"""))
        }

        assertEquals(AppVersionInfo("2.1", "2.3", ""), gson.fromJson(json, AppVersionInfo::class.java))
    }

    @Test
    fun `typed version info and legacy version use separate unauthenticated GET routes and response shapes`() =
        withServer { server, api ->
            server.enqueue(response("""{"success":true,"data":$VALID_INFO,"error":null}"""))

            val info = runBlocking { api.appVersionInfo() }

            assertTrue(info.success)
            assertEquals(AppVersionInfo("2.1", "2.3", ""), info.data)
            val infoRequest = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("GET", infoRequest.method)
            assertEquals("/api/app/version-info", infoRequest.path)
            assertEquals(0L, infoRequest.bodySize)
            assertNull(infoRequest.getHeader("Authorization"))

            server.enqueue(response("""{"success":true,"data":"2.3","error":null}"""))

            val legacy = runBlocking { api.latestAppVersion() }

            assertTrue(legacy.success)
            assertEquals("2.3", legacy.data)
            val legacyRequest = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("GET", legacyRequest.method)
            assertEquals("/api/app/version", legacyRequest.path)
            assertEquals(0L, legacyRequest.bodySize)
            assertNull(legacyRequest.getHeader("Authorization"))
        }

    @Test
    fun `failure response may contain null version data without violating strict field parsing`() = withServer { server, api ->
        server.enqueue(response("""{
            "success":false,
            "data":null,
            "error":{"message":"Version information is unavailable","code":"unavailable"}
        }"""))

        val result = runBlocking { api.appVersionInfo() }

        assertFalse(result.success)
        assertNull(result.data)
        assertEquals("Version information is unavailable", result.error?.message)
        assertEquals("unavailable", result.error?.code)
        val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        assertEquals("GET", request.method)
        assertEquals("/api/app/version-info", request.path)
        assertNull(request.getHeader("Authorization"))
    }

    private fun response(body: String) = MockResponse()
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private fun withServer(block: (MockWebServer, ItmoWidgetsApi) -> Unit) {
        MockWebServer().use { server ->
            val client = object : ItmoWidgetsImpl(MyItmo(), server.url("/").toString()) {
                override fun getValidToken(): String? = null
            }
            block(server, client.api)
        }
    }

    private companion object {
        val FIELDS = listOf("minVersion", "latestVersion", "note")
        const val VALID_INFO = """{"minVersion":"2.1","latestVersion":"2.3","note":""}"""
    }
}
