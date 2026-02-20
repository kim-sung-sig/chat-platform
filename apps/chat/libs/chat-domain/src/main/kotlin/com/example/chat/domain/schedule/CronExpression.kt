package com.example.chat.domain.schedule

/**
 * 단순한 Cron 표현식 래퍼 (테스트용 최소 구현)
 */
class CronExpression(private val value: String) {
    fun getValue(): String = value

    companion object {
        @JvmStatic
        fun of(value: String): CronExpression = CronExpression(value)
    }
}
