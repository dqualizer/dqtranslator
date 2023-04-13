package de.dqualizer.dqtranslator.messaging

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfiguration {
    @Value("\${dqualizer.rabbitmq.loadtestExchange}")
    private lateinit var loadTestExchangeName: String

    @Value("\${dqualizer.rabbitmq.loadtestQueue}")
    private lateinit var loadTestQueueName: String

    @Value("\${dqualizer.rabbitmq.rqaDefinitionQueue}")
    private lateinit var rqaDefinitionQueueName: String

    @Value("\${dqualizer.rabbitmq.rqaExchange}")
    private lateinit var rqaExchangeName: String

    private val postKey = "post"
    private val getKey = "get"

    @Bean
    fun loadTestExchange(): TopicExchange {
        return TopicExchange(loadTestExchangeName)
    }

    @Bean
    fun loadTestQueue(): Queue {
        return Queue(loadTestQueueName, false)
    }

    @Bean
    fun loadTestBinding(@Qualifier("loadTestQueue") queue: Queue?,
                        @Qualifier("loadTestExchange") exchange: TopicExchange?): Binding? {
        return BindingBuilder.bind(queue).to(exchange).with(postKey)
    }

    @Bean
    fun modelingQueue(): Queue {
        return Queue(rqaDefinitionQueueName, false)
    }

    @Bean
    fun modelingExchange(): TopicExchange {
        return TopicExchange(rqaExchangeName)
    }

    @Bean
    fun modelingBinding(@Qualifier("modelingQueue") queue: Queue?,
                        @Qualifier("modelingExchange") exchange: TopicExchange?): Binding? {
        return BindingBuilder.bind(queue).to(exchange).with(getKey)
    }

    @Bean
    fun messageConverter(): MessageConverter? {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun template(connectionFactory: ConnectionFactory?): AmqpTemplate? {
        val template = RabbitTemplate(connectionFactory!!)
        template.messageConverter = messageConverter()!!
        return template
    }
}