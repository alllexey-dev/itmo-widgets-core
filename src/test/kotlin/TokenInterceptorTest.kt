import com.google.gson.Gson
import dev.alllexey.itmowidgets.core.ItmoWidgets
import dev.alllexey.itmowidgets.core.ItmoWidgetsApi
import dev.alllexey.itmowidgets.core.utils.TokenInterceptor
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit

class TokenInterceptorTest {

    @Test
    fun `adds a bearer token when one is available`() = withServer { server ->
        val client = client { "access-token" }
        server.enqueue(MockResponse())

        client.newCall(Request.Builder().url(server.url("/private")).build())
            .execute()
            .close()

        assertEquals(
            "Bearer access-token",
            server.takeRequest().getHeader("Authorization")
        )
    }

    @Test
    fun `leaves public requests unauthenticated when no token is available`() =
        withServer { server ->
            val client = client { null }
            server.enqueue(MockResponse())

            client.newCall(Request.Builder().url(server.url("/public")).build())
                .execute()
                .close()

            assertNull(server.takeRequest().getHeader("Authorization"))
        }

    @Test
    fun `does not silently downgrade a failed token refresh`() = withServer { server ->
        val client = client { throw IllegalStateException("refresh failed") }

        assertFailsWith<IOException> {
            client.newCall(Request.Builder().url(server.url("/private")).build())
                .execute()
        }
        assertEquals(0, server.requestCount)
    }

    private fun client(tokenProvider: () -> String?): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(FakeItmoWidgets(tokenProvider)))
            .build()

    private fun withServer(block: (MockWebServer) -> Unit) {
        MockWebServer().use(block)
    }

    private class FakeItmoWidgets(
        private val tokenProvider: () -> String?
    ) : ItmoWidgets {
        override val api: ItmoWidgetsApi
            get() = error("Not used")
        override val moderationApi: dev.alllexey.itmowidgets.core.ItmoWidgetsModerationApi
            get() = error("Not used")
        override val retrofit: Retrofit
            get() = error("Not used")
        override val okHttpClient: OkHttpClient
            get() = error("Not used")
        override val gson: Gson
            get() = error("Not used")

        override fun getValidToken(): String? = tokenProvider()
    }
}
