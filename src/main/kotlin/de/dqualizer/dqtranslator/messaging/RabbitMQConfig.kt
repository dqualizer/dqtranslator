package de.dqualizer.dqtranslator.messaging

import org.springframework.amqp.core.Queue
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RabbitMQConfig {

    @Value("\${dqualizer.messaging.queues.k6.back-mapping}")
    private lateinit var queue: String
    @Bean
    fun createK6BackMappingConsumerQueue(): Queue {
        return Queue(queue)
    }

    @Bean
    fun converter(): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter()
    }
}