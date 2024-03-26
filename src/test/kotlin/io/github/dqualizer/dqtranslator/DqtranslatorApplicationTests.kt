package io.github.dqualizer.dqtranslator

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import


@SpringBootTest
@Import(MockRabbitMQConnection::class)
class DqtranslatorApplicationTests {

    @Test
    fun contextLoads() {
    }
}


