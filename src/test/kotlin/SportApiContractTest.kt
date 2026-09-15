import api.myitmo.MyItmo
import dev.alllexey.itmowidgets.core.ItmoWidgetsApi
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.FriendSportBooking
import dev.alllexey.itmowidgets.core.model.FriendsSportBookingsResponse
import dev.alllexey.itmowidgets.core.model.QueueEntryStatus
import dev.alllexey.itmowidgets.core.model.SportAutoSignEntry
import dev.alllexey.itmowidgets.core.model.SportFreeSignEntry
import dev.alllexey.itmowidgets.core.model.SportLessonDto
import dev.alllexey.itmowidgets.core.model.SportQueueEntry
import dev.alllexey.itmowidgets.core.model.UserSportBookingsResponse
import com.google.gson.JsonParseException
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import retrofit2.http.GET

class SportApiContractTest {

    private val gson = ItmoWidgetsImpl(MyItmo()).gson
    private val timestamp = OffsetDateTime.parse("2026-09-07T12:00:00+03:00")
    private val lesson = SportLessonDto(
        id = 100,
        sectionId = 200,
        sectionName = "Бадминтон",
        sectionLevel = 1,
        level = 1,
        typeId = 2,
        buildingId = 300,
        roomName = "Большой зал",
        start = timestamp,
        end = timestamp.plusMinutes(90),
        timeSlotId = 400,
        teacherIsu = 123456,
        teacherFio = "Иванов Иван"
    )

    @Test
    fun `friends sport bookings use the expected GET route`() {
        val method = ItmoWidgetsApi::class.java.declaredMethods
            .single { it.name == "friendsSportBookings" }
        val get = method.getAnnotation(GET::class.java)

        assertEquals("/api/sport/friends/sport-bookings", get.value)
    }

    @Test
    fun `friend booking queue entry subtypes round trip`() {
        val source = FriendsSportBookingsResponse(
            bookings = listOf(
                FriendSportBooking(
                    isu = 111111,
                    lessonId = lesson.id,
                    entry = freeEntry()
                ),
                FriendSportBooking(
                    isu = 222222,
                    lessonId = lesson.id,
                    entry = autoEntry()
                )
            )
        )

        val restored = gson.fromJson(
            gson.toJson(source),
            FriendsSportBookingsResponse::class.java
        )

        assertIs<SportFreeSignEntry>(restored.bookings[0].entry)
        assertIs<SportAutoSignEntry>(restored.bookings[1].entry)
        assertEquals(source, restored)
    }

    @Test
    fun `raw external and nullable online venues round trip in both queue types`() {
        for (building in listOf<Long?>(335L, 493L, null, -1L)) {
            val venue = lesson.copy(buildingId = building, roomName = if (building == null || building == -1L) "Online" else "External venue address")
            val entries = listOf(
                (freeEntry() as SportFreeSignEntry).copy(targetLesson = venue),
                (autoEntry() as SportAutoSignEntry).copy(targetLesson = venue, realLesson = venue),
            )
            for (source in entries) {
                val json = gson.toJsonTree(source).asJsonObject
                // Explicit null matches Spring/Jackson; absent null also occurs with Gson defaults.
                json.getAsJsonObject("targetLesson").add("buildingId", gson.toJsonTree(building))
                val restored = gson.fromJson(json, SportQueueEntry::class.java)
                assertEquals(source, restored)
                assertEquals(building, restored.targetLesson.buildingId)
            }
        }
    }

    @Test
    fun `target sport response decodes confirmed IDs and both active queue types through Retrofit`() {
        MockWebServer().use { server ->
            val client = object : ItmoWidgetsImpl(MyItmo(), server.url("/").toString()) {
                override fun getValidToken(): String? = null
            }
            val source = UserSportBookingsResponse(listOf(2147483648L), listOf(freeEntry(), autoEntry()))
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json")
                .setBody("""{"success":true,"data":${gson.toJson(source)},"error":null}"""))
            val result = runBlocking { client.api.userSportBookings(123456) }
            assertEquals(source, result.data)
            assertIs<SportFreeSignEntry>(result.data?.entries?.get(0))
            assertIs<SportAutoSignEntry>(result.data?.entries?.get(1))
            val request = assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("GET", request.method)
            assertEquals("/api/sport/users/123456/bookings", request.path)
        }
    }

    @Test
    fun `confirmed-only server safely defaults missing entries and malformed arrays are rejected`() {
        assertEquals(UserSportBookingsResponse(listOf(100L)),
            gson.fromJson("""{"lessonIds":[100]}""", UserSportBookingsResponse::class.java))
        for (json in listOf("{}", """{"lessonIds":null}""", """{"lessonIds":[],"entries":null}""",
            """{"lessonIds":[],"entries":{}}""", """{"lessonIds":[],"entries":[{"type":"unknown"}]}""")) {
            assertFailsWith<JsonParseException> { gson.fromJson(json, UserSportBookingsResponse::class.java) }
        }
    }

    private fun freeEntry(): SportQueueEntry = SportFreeSignEntry(
        id = 1,
        lessonId = lesson.id,
        position = 1,
        total = 2,
        isCancelled = false,
        status = QueueEntryStatus.WAITING,
        createdAt = timestamp,
        firstNotifiedAt = null,
        lastNotifiedAt = null,
        cancelledAt = null,
        satisfiedAt = null,
        expiredAt = null,
        notificationAttempts = 0,
        maxNotificationAttempts = 10,
        targetLesson = lesson,
        forceSign = false
    )

    private fun autoEntry(): SportQueueEntry = SportAutoSignEntry(
        id = 2,
        prototypeLessonId = lesson.id,
        realLessonId = null,
        position = 2,
        total = 3,
        isCancelled = false,
        status = QueueEntryStatus.NOTIFIED,
        createdAt = timestamp,
        firstNotifiedAt = timestamp,
        lastNotifiedAt = timestamp,
        cancelledAt = null,
        satisfiedAt = null,
        expiredAt = null,
        notificationAttempts = 1,
        maxNotificationAttempts = 10,
        targetLesson = lesson,
        realLesson = null
    )
}
