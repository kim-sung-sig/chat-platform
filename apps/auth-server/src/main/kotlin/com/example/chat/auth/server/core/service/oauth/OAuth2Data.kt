
package com.example.chat.auth.server.core.service.oauth

interface OAuth2Data {
    fun getProvider(): String
    fun getProviderId(): String
    fun getEmail(): String?
    fun getName(): String?
    fun getNickName(): String?
}
