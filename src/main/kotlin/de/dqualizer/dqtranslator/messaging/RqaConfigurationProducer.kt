package de.dqualizer.dqtranslator.messaging

import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.ResilienceTestConfiguration
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RQAConfigurationProducer(
        val template: RabbitTemplate,
        val messageConverter: MessageConverter
) {

    @Value("\${dqualizer.messaging.queues.rqaConfigurationProducerQueue.name}")
    private lateinit var producerQueueRoutingKey: String


    fun produceLoadtestConfig(loadtestConfiguration: LoadTestConfiguration) {
        val message = messageConverter.toMessage(loadtestConfiguration,MessageProperties().apply {  })
        template.convertAndSend(producerQueueRoutingKey, message)
    }

    fun produceResilienceTestConfig(resilienceTestConfig: ResilienceTestConfiguration) {
        val message = messageConverter.toMessage(resilienceTestConfig,MessageProperties().apply {  })
        template.convertAndSend(producerQueueRoutingKey, message)
    }
}