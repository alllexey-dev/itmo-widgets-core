package dev.alllexey.itmowidgets.core.model

import java.time.OffsetDateTime
import java.util.UUID

/**
 * A pending browser sign-in shown in the app before the user approves it
 * (`GET /api/users/me/web-login/{code}`). Everything except [userAgent] is
 * required on the wire.
 *
 * @property challengeId The challenge to pass to `approveWebLogin`.
 * @property userAgent The browser's User-Agent as Backend recorded it, if any.
 */
data class WebLoginPreview(
    val challengeId: UUID,
    val userAgent: String?,
    val createdAt: OffsetDateTime,
    val expiresAt: OffsetDateTime,
)
