package io.github.dqualizer.dqtranslator.messaging

import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.MessageHeaders
import org.springframework.stereotype.Component

@Component
class RQAConfigurationProducer(
    val template: RabbitTemplate,
    val messageConverter: MessageConverter
) {

    @Value("\${dqualizer.messaging.queues.rqaConfigurationProducerQueue.name}")
    private lateinit var producerQueueRoutingKey: String

    fun produce(rqaConfiguration: RQAConfiguration, headers: MessageHeaders) {
        val message = messageConverter.toMessage(rqaConfiguration, MessageProperties().apply { setHeaders(headers) })
        template.convertAndSend(producerQueueRoutingKey, message)
    }
}
