package dev.alllexey.itmowidgets.core.utils

import dev.alllexey.itmowidgets.core.ItmoWidgets
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException

class TokenInterceptor(
    private val itmoWidgets: ItmoWidgets,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        try {
            val token = itmoWidgets.getValidToken()
            val newRequest = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        } catch (e: Exception) {
            throw IOException("Failed to add auth token to request", e)
        }
    }
}