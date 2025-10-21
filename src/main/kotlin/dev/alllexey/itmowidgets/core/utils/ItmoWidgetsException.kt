package dev.alllexey.itmowidgets.core.utils

open class ItmoWidgetsException(message: String, cause: Throwable? = null) : Exception(message, cause)

class AuthenticationException(message: String, cause: Throwable? = null) : ItmoWidgetsException(message, cause)

class NetworkException(message: String, cause: Throwable? = null) : ItmoWidgetsException(message, cause)