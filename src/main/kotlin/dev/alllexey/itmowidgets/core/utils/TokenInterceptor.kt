package dev.alllexey.itmowidgets.core.utils

import dev.alllexey.itmowidgets.core.ItmoWidgets
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(
    private val itmoWidgets: ItmoWidgets,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = try {
            itmoWidgets.getValidToken()?.trim()?.takeIf(String::isNotEmpty)
        } catch (error: Exception) {
            throw IOException("Failed to add auth token to request", error)
        }
        val authenticatedRequest = token?.let {
            request.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        } ?: request

        return chain.proceed(authenticatedRequest)
    }
}
