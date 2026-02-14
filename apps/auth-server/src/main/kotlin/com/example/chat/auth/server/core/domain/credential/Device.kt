
package com.example.chat.auth.server.core.domain.credential

import jakarta.persistence.Embeddable

@Embeddable
data class Device(
    val deviceId: String? = null,
    val platform: String? = null,
    val browser: String? = null
) {
    fun isWeb(): Boolean = "WEB".equals(platform, ignoreCase = true)

    fun isSameDevice(other: Device?): Boolean {
        if (other == null) return false
        return deviceId == other.deviceId && platform == other.platform && browser == other.browser
    }
}
