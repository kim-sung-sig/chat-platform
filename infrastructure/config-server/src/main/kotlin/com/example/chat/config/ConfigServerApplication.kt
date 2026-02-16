package com.example.chat.config

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.config.server.EnableConfigServer

/**
 * Spring Cloud Config Server
 *
 * 모든 마이크로서비스의 설정을 중앙에서 관리합니다.
 * Git Repository 또는 파일 시스템 기반 설정 제공
 */
@EnableConfigServer
@SpringBootApplication
class ConfigServerApplication

fun main(args: Array<String>) {
	runApplication<ConfigServerApplication>(*args)
}
