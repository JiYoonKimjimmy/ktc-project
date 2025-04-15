package com.kona.ktca

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = [
	"com.kona.ktca",
	"com.kona.common"
])
class KtcaApplication

fun main(args: Array<String>) {
	runApplication<KtcaApplication>(*args)
}
