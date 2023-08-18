package io.github.dqualizer.dqtranslator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication(scanBasePackages = ["io.github.dqualizer"])
class DqtranslatorApplication

fun main(args: Array<String>) {
	runApplication<DqtranslatorApplication>(*args)
}
