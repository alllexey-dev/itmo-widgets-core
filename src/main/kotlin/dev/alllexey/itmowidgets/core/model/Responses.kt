package dev.alllexey.itmowidgets.core.model

import java.time.Instant
import java.util.UUID

data class TokenResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long
)

data class SessionResponse(val tokenId: UUID, val lastUsed: Instant)