package com.example.chat.eureka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

/**
 * Spring Cloud Eureka Server
 *
 * 서비스 디스커버리 서버
 * 모든 마이크로서비스가 자신을 등록하고 다른 서비스를 찾을 수 있습니다.
 */
@EnableEurekaServer
@SpringBootApplication
class EurekaServerApplication

fun main(args: Array<String>) {
	runApplication<EurekaServerApplication>(*args)
}
