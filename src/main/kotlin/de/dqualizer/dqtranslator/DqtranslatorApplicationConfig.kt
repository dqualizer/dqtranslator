package de.dqualizer.dqtranslator

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DqtranslatorApplicationConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }
}