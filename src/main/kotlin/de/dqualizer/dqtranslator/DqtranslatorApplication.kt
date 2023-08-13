package de.dqualizer.dqtranslator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("de.dqualizer.dqtranslator", "io.github.dqualizer.dqlang")
class DqtranslatorApplication

fun main(args: Array<String>) {
	runApplication<DqtranslatorApplication>(*args)
}
