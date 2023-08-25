package io.github.dqualizer.dqtranslator

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DqtranslatorApplicationTests {

    @Autowired
    lateinit var buildProperties: BuildProperties;

    @Test
    fun contextLoads() {
        println(buildProperties)
    }

}
