package dev.alllexey.itmowidgets.core.utils

// not recommended for production
class RuntimeStorage : ItmoWidgetsStorage {

    private var accessToken: String? = null

    private var refreshToken: String? = null

    private var accessExpiresAt: Long = 0

    private var refreshExpiresAt: Long = 0

    override fun getAccessToken() = accessToken

    override fun getAccessExpiresAt() = accessExpiresAt

    override fun getRefreshToken() = refreshToken

    override fun getRefreshExpiresAt() = refreshExpiresAt

    override fun setAccessToken(accessToken: String?) {
        this.accessToken = accessToken
    }

    override fun setAccessExpiresAt(accessExpiresAt: Long) {
        this.accessExpiresAt = accessExpiresAt
    }

    override fun setRefreshToken(refreshToken: String?) {
        this.refreshToken = refreshToken
    }

    override fun setRefreshExpiresAt(refreshExpiresAt: Long) {
        this.refreshExpiresAt = refreshExpiresAt
    }
}