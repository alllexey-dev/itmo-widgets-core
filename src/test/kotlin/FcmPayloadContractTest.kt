import api.myitmo.MyItmo
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.UserCapabilities
import dev.alllexey.itmowidgets.core.model.UserData
import dev.alllexey.itmowidgets.core.model.fcm.FcmJsonWrapper
import dev.alllexey.itmowidgets.core.model.fcm.FcmTypedWrapper
import dev.alllexey.itmowidgets.core.model.fcm.impl.FriendshipEvent
import dev.alllexey.itmowidgets.core.model.fcm.impl.FriendshipEventPayload
import java.time.OffsetDateTime
import kotlin.test.*

class FcmPayloadContractTest {
    private val gson = ItmoWidgetsImpl(MyItmo()).gson
    private val actor = UserData(100001, "Synthetic actor", null, emptyList(), UserCapabilities(false, true))
    private val time = OffsetDateTime.parse("2026-09-15T12:34:56+03:00")

    @Test
    fun `both events preserve the data wrapper actor capabilities and offset time`() {
        for (event in FriendshipEvent.entries) {
            val payload = FriendshipEventPayload(event, actor, time)
            val wire = gson.toJson(FcmTypedWrapper(payload.getType(), payload))
            val wrapper = gson.fromJson(wire, FcmJsonWrapper::class.java)
            assertEquals(FriendshipEventPayload.TYPE, wrapper.type)
            assertEquals(payload, gson.fromJson(wrapper.payload, FriendshipEventPayload::class.java))
            assertEquals(setOf("type", "payload"), JsonParser.parseString(wire).asJsonObject.keySet())
            assertEquals(setOf("event", "user", "occurredAt"), wrapper.payload.asJsonObject.keySet())
            assertFalse(wire.contains("settings"))
        }
    }

    @Test
    fun `unknown event and incomplete payload fail closed`() {
        val valid = gson.toJsonTree(FriendshipEventPayload(FriendshipEvent.REQUEST_RECEIVED, actor, time)).asJsonObject
        for (field in listOf("event", "user", "occurredAt")) {
            val missing = valid.deepCopy().apply { remove(field) }
            assertFailsWith<JsonParseException> { gson.fromJson(missing, FriendshipEventPayload::class.java) }
            val nil = valid.deepCopy().apply { add(field, com.google.gson.JsonNull.INSTANCE) }
            assertFailsWith<Exception> { gson.fromJson(nil, FriendshipEventPayload::class.java) }
        }
        for (event in listOf("UNKNOWN", "request_received", "")) {
            val unknown = valid.deepCopy().apply { addProperty("event", event) }
            assertFailsWith<JsonParseException> { gson.fromJson(unknown, FriendshipEventPayload::class.java) }
        }
        for (wire in listOf("null", "[]", "42", "{}")) {
            assertFailsWith<JsonParseException> { gson.fromJson(wire, FriendshipEventPayload::class.java) }
        }
    }
}
