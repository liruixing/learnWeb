package com.dennis.learn

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class LearnApplication

fun main(args: Array<String>) {
	runApplication<LearnApplication>(*args)
}
