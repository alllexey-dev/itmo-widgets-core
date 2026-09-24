package weblogin

import api.myitmo.MyItmo
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.WebLoginPreview
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.*

class WebLoginContractTest {
    private val gson = ItmoWidgetsImpl(MyItmo()).gson
    private val preview = WebLoginPreview(UUID.fromString("00000000-0000-0000-0000-000000000077"),
        "Mozilla/5.0 (X11; Linux x86_64)", OffsetDateTime.parse("2026-09-24T10:00:00Z"),
        OffsetDateTime.parse("2026-09-24T10:05:00Z"))

    @Test
    fun `web login preview round trips with and without a user agent`() {
        for (value in listOf(preview, preview.copy(userAgent = null))) {
            assertEquals(value, gson.fromJson(gson.toJson(value), WebLoginPreview::class.java))
        }
    }

    @Test
    fun `web login preview uses the exact wire field names and decodes Backend instants`() {
        assertEquals(setOf("challengeId", "userAgent", "createdAt", "expiresAt"), gson.toJsonTree(preview).asJsonObject.keySet())
        val wire = """{"challengeId":"00000000-0000-0000-0000-000000000077","userAgent":"Mozilla/5.0 (X11; Linux x86_64)",
            "createdAt":"2026-09-24T10:00:00.123456Z","expiresAt":"2026-09-24T10:05:00Z"}"""
        assertEquals(preview.copy(createdAt = OffsetDateTime.parse("2026-09-24T10:00:00.123456Z")),
            gson.fromJson(wire, WebLoginPreview::class.java))
    }

    @Test
    fun `user agent decodes from both null and absence`() {
        val expected = preview.copy(userAgent = null)
        val explicitNull = gson.toJsonTree(preview).asJsonObject.apply { add("userAgent", JsonNull.INSTANCE) }
        assertEquals(expected, gson.fromJson(explicitNull, WebLoginPreview::class.java))
        val absent = gson.toJsonTree(preview).asJsonObject.apply { remove("userAgent") }
        assertEquals(expected, gson.fromJson(absent, WebLoginPreview::class.java))
    }

    @Test
    fun `required web login fields never become JVM defaults`() {
        for (name in listOf("challengeId", "createdAt", "expiresAt")) {
            val missing = gson.toJsonTree(preview).asJsonObject.apply { remove(name) }
            assertFailsWith<JsonParseException>("$name missing") { gson.fromJson(missing, WebLoginPreview::class.java) }
            val nullValue = gson.toJsonTree(preview).asJsonObject.apply { add(name, JsonNull.INSTANCE) }
            assertFailsWith<JsonParseException>("$name null") { gson.fromJson(nullValue, WebLoginPreview::class.java) }
            val number = gson.toJsonTree(preview).asJsonObject.apply { addProperty(name, 1) }
            assertFailsWith<JsonParseException>("$name number") { gson.fromJson(number, WebLoginPreview::class.java) }
        }
    }

    @Test
    fun `malformed web login values fail decoding`() {
        val malformed = listOf("challengeId" to "\"not-a-uuid\"", "userAgent" to "42", "userAgent" to "true", "userAgent" to "{}",
            "userAgent" to "[]", "createdAt" to "\"yesterday\"", "expiresAt" to "\"2026-09-24\"")
        for ((field, wire) in malformed) {
            val json = gson.toJsonTree(preview).asJsonObject.apply { add(field, JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>("$field: $wire") { gson.fromJson(json, WebLoginPreview::class.java) }
        }
        val duplicate = gson.toJson(preview).dropLast(1) + ",\"challengeId\":\"00000000-0000-0000-0000-000000000078\"}"
        assertFailsWith<JsonParseException> { gson.fromJson(duplicate, WebLoginPreview::class.java) }
        for (wire in listOf("[]", "\"preview\"", "1")) {
            assertFailsWith<JsonParseException>(wire) { gson.fromJson(wire, WebLoginPreview::class.java) }
        }
    }
}
