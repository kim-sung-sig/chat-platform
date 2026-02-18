package com.example.chat.system

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChatSystemServerApplication

fun main(args: Array<String>) {
	runApplication<ChatSystemServerApplication>(*args)
}
