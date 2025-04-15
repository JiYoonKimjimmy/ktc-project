package com.kona.ktc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
class KtcApplication

fun main(args: Array<String>) {
	runApplication<KtcApplication>(*args)
}
