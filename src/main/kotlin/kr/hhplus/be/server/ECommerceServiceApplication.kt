package kr.hhplus.be.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class ECommerceServiceApplication

fun main(args: Array<String>) {
	runApplication<ECommerceServiceApplication>(*args)
}