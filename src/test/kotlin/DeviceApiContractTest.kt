import api.myitmo.MyItmo
import dev.alllexey.itmowidgets.core.ItmoWidgetsApi
import dev.alllexey.itmowidgets.core.model.UnregisterDeviceRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import retrofit2.http.Body
import retrofit2.http.HTTP

class DeviceApiContractTest {

    @Test
    fun `unregister current device uses a delete request body`() {
        val method = ItmoWidgetsApi::class.java.declaredMethods
            .single { it.name == "unregisterCurrentDevice" }
        val http = assertNotNull(method.getAnnotation(HTTP::class.java))

        assertEquals("DELETE", http.method)
        assertEquals("/api/device/current", http.path)
        assertEquals(true, http.hasBody)
        assertEquals(
            1,
            method.parameterAnnotations.flatten().count { it is Body }
        )
    }

    @Test
    fun `unregister request keeps the wire field name`() {
        val json = MyItmo().gson.toJson(UnregisterDeviceRequest("test-token"))

        assertEquals("{\"fcmToken\":\"test-token\"}", json)
    }
}
