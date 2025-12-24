package dev.alllexey.itmowidgets.core.utils

import dev.alllexey.itmowidgets.core.ItmoWidgets
import dev.alllexey.itmowidgets.core.model.ApiResponse
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException

class TokenInterceptor(
    private val itmoWidgets: ItmoWidgets,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.pathSegments.contains("refresh")) {
            val response = chain.proceed(request)
            if (response.code != 409) {
                return response
            }

            try {
                val errorMessage = response.body!!.string()
                    .let { body -> itmoWidgets.gson().fromJson(body, ApiResponse::class.java) }.error!!.message
                if (!errorMessage.contains("refresh token", ignoreCase = true)) {
                    return response
                }
            } catch (_: Exception) {
                return response
            }

            response.close()
            itmoWidgets.loginBlocking()

            throw IOException("Failed to refresh tokens, re-login performed")
        }

        if (request.url.pathSegments.contains("auth")) {
            return chain.proceed(request)
        }

        try {
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