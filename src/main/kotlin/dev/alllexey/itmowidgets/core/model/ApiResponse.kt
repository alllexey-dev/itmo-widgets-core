package dev.alllexey.itmowidgets.core.model

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorDetails?
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data, error = null)
        }

        fun error(message: String, code: String? = null): ApiResponse<Unit> {
            return ApiResponse(success = false, data = null, error = ErrorDetails(message, code))
        }
    }
}