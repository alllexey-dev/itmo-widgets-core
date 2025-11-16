package dev.alllexey.itmowidgets.core.model

import java.time.Instant
import java.util.*

data class TokenResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long
)

data class SessionResponse(val tokenId: UUID, val lastUsed: Instant)
data class SportFilterResponse(
    val id: Long,
    val sectionIds: List<Long>,
    val buildingIds: List<Long>,
    val timeSlotIds: List<Long>,
    val teacherIds: List<Long>,
)

data class SportFreeSignEntry(
    val id: Long,
    val lessonId: Long,
    val position: Int,
    val total: Int
)