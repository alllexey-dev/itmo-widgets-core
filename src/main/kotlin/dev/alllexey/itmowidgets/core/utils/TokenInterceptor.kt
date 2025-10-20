package dev.alllexey.itmowidgets.core.utils

import dev.alllexey.itmowidgets.core.ItmoWidgets
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException

class TokenInterceptor(
    private val itmoWidgets: ItmoWidgets,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.pathSegments.contains("auth")) {
            return chain.proceed(request)
        }

        try {
            // Use runBlocking for the interceptor context, as Interceptors are not suspendable by default.
            val validTokens = runBlocking { itmoWidgets.getValidTokensAsync().getOrThrow() }
            val newRequest = request.newBuilder()
                .header("Authorization", "Bearer ${validTokens.accessToken}")
                .build()
            return chain.proceed(newRequest)
        } catch (e: Exception) {
            throw IOException("Failed to add auth token to request", e)
        }
    }
}