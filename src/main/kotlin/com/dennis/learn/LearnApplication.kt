package com.dennis.learn

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LearnApplication

fun main(args: Array<String>) {
	runApplication<LearnApplication>(*args)
}
