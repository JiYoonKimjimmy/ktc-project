package com.kona.ktc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = [
	"com.kona.ktc",
	"com.kona.common"
])
class KtcApplication

fun main(args: Array<String>) {
	runApplication<KtcApplication>(*args)
}
