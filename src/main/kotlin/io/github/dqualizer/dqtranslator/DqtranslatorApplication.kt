package io.github.dqualizer.dqtranslator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DqtranslatorApplication

fun main(args: Array<String>) {
  runApplication<DqtranslatorApplication>(*args)
}
