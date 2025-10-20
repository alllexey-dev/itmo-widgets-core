package dev.alllexey.itmowidgets.core.utils

import dev.alllexey.itmowidgets.core.model.TokenResponse

interface ItmoWidgetsStorage {

    fun getAccessToken(): String?

    fun getAccessExpiresAt(): Long

    fun getRefreshToken(): String?

    fun getRefreshExpiresAt(): Long

    fun setAccessToken(accessToken: String?)

    fun setAccessExpiresAt(accessExpiresAt: Long)

    fun setRefreshToken(refreshToken: String?)

    fun setRefreshExpiresAt(refreshExpiresAt: Long)

    fun updateTokens(response: TokenResponse) {
        val currentMillis = System.currentTimeMillis()
        setAccessToken(response.accessToken)
        setAccessExpiresAt(currentMillis + response.accessTokenExpiresIn)
        setRefreshToken(response.refreshToken)
        setRefreshExpiresAt(currentMillis + response.refreshTokenExpiresIn)
    }

    fun toTokenResponse(): TokenResponse {
        return TokenResponse(
            accessToken = getAccessToken() ?: "",
            accessTokenExpiresIn = 0,
            refreshToken = getRefreshToken() ?: "",
            refreshTokenExpiresIn = 0
        )
    }

    fun clearTokens() {
        setAccessToken(null)
        setAccessExpiresAt(0)
        setRefreshToken(null)
        setRefreshExpiresAt(0)
    }
}