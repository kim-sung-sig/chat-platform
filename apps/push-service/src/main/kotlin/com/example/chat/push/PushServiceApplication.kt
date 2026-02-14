package com.example.chat.push

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PushServiceApplication

fun main(args: Array<String>) {
    runApplication<PushServiceApplication>(*args)
}
