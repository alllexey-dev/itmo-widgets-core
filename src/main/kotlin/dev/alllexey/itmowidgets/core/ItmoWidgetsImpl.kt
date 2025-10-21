package dev.alllexey.itmowidgets.core

import api.myitmo.MyItmo
import com.google.gson.Gson
import dev.alllexey.itmowidgets.core.utils.ItmoWidgetsStorage
import dev.alllexey.itmowidgets.core.utils.TokenInterceptor
import dev.alllexey.itmowidgets.core.model.ItmoTokenLoginRequest
import dev.alllexey.itmowidgets.core.model.RefreshTokenRequest
import dev.alllexey.itmowidgets.core.model.TokenResponse
import dev.alllexey.itmowidgets.core.utils.AuthenticationException
import dev.alllexey.itmowidgets.core.utils.InstantTypeAdapter
import dev.alllexey.itmowidgets.core.utils.ItmoWidgetsException
import dev.alllexey.itmowidgets.core.utils.NetworkException
import dev.alllexey.itmowidgets.core.utils.RuntimeStorage
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.Instant

open class ItmoWidgetsImpl(
    val myItmo: MyItmo,
    val storage: ItmoWidgetsStorage = RuntimeStorage(),
    private val baseUrl: String = "https://widgets.alllexey.dev"
) : ItmoWidgets {

    protected val api: ItmoWidgetsApi by lazy {
        retrofit.create(ItmoWidgetsApi::class.java)
    }

    protected val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .client(okHttpClient())
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson()))
            .build()
    }

    protected val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(this))
            .build()
    }

    protected val gson: Gson by lazy {
        myItmo.gson.newBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
            .create()
    }

    override fun api() = api

    override fun retrofit() = retrofit

    override fun okHttpClient() = okHttpClient

    override fun gson() = gson

    override fun storage() = storage

    private val validTokensLock = Any()

    override fun getValidTokens(): TokenResponse {
        if (needsLogin() || needsRefresh()) {
            synchronized(validTokensLock) {
                if (needsLogin() || needsRefresh()) {
                    return refreshTokensBlocking()
                }
            }
        }
        return storage.toTokenResponse()
    }

    override fun refreshTokensBlocking(): TokenResponse = runBlocking {
        refreshTokens().getOrThrow()
    }

    override fun loginBlocking(): TokenResponse = runBlocking {
        login().getOrThrow()
    }

    override suspend fun getValidTokensAsync(): Result<TokenResponse> {
        if (needsLogin() || needsRefresh()) {
            return refreshTokens()
        }
        return Result.success(storage.toTokenResponse())
    }

    override suspend fun refreshTokens(): Result<TokenResponse> {
        val refreshToken = storage.getRefreshToken()
        if (refreshToken == null) return login()

        return try {
            val response = api.refreshToken(RefreshTokenRequest(refreshToken))
            response.data?.let {
                storage.updateTokens(it)
                Result.success(it)
            } ?: Result.failure(AuthenticationException("Could not refresh backend tokens. Server response body is null."))
        } catch (e: IOException) {
            Result.failure(NetworkException("Could not refresh tokens due to network error", e))
        } catch (e: Exception) {
            Result.failure(ItmoWidgetsException("An unexpected error occurred during token refresh", e))
        }
    }

    override suspend fun login(): Result<TokenResponse> {
        val myItmoTokens = try {
            myItmo.validTokens
        } catch (e: Exception) {
            return Result.failure(AuthenticationException("Could not login using ITMO ID token", e))
        }

        val accessToken = myItmoTokens.accessToken
            ?: return Result.failure(AuthenticationException("Could not login. ITMO ID access token is null after refresh."))

        return try {
            val response = api.loginViaItmoToken(ItmoTokenLoginRequest(accessToken))
            response.data?.let {
                storage.updateTokens(it)
                Result.success(it)
            } ?: Result.failure(AuthenticationException("Could not login. Server response body is null."))
        } catch (e: IOException) {
            Result.failure(NetworkException("Could not login due to network error", e))
        } catch (e: Exception) {
            Result.failure(ItmoWidgetsException("An unexpected error occurred during login", e))
        }
    }

    fun needsLogin(): Boolean = !hasRefreshToken() || isRefreshTokenExpired()
    fun needsRefresh(): Boolean = !hasAccessToken() || isAccessTokenExpired()
    fun hasAccessToken(): Boolean = storage.getAccessToken() != null
    fun hasRefreshToken(): Boolean = storage.getRefreshToken() != null
    fun isAccessTokenExpired(): Boolean = storage.getAccessExpiresAt() < System.currentTimeMillis()
    fun isRefreshTokenExpired(): Boolean = storage.getRefreshExpiresAt() < System.currentTimeMillis()
}