package dev.alllexey.itmowidgets.core.model

/**
 * Android application release metadata from `/api/app/version-info`, independent
 * of Backend and Core library versions. All three wire fields are required.
 *
 * @property minVersion The oldest application version supported by this Backend.
 * @property latestVersion The latest application release; also returned as the
 * legacy string by `/api/app/version`.
 * @property note Plain text for a future update notice, not HTML or Markdown.
 * It may be empty.
 *
 * This DTO does not compare versions or enforce an application update policy.
 */
data class AppVersionInfo(
    val minVersion: String,
    val latestVersion: String,
    val note: String
)
