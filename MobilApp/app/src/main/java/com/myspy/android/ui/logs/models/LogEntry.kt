package com.myspy.android.ui.logs.models

/**
 * Log Entry data class
 * @author Michael KOJDL
 * @version 1.0.0
 */
data class LogEntry(
    val level: LogLevel,
    val message: String,
    val timestamp: Long
)

/**
 * Log Level enum
 */
enum class LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR
}
