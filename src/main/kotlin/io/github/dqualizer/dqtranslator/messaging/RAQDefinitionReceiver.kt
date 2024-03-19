package io.github.dqualizer.dqtranslator.messaging


import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqtranslator.translation.TranslationService
import mu.KotlinLogging
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class RAQDefinitionReceiver(
    private val translationService: TranslationService,
    private val rqaConfigurationProducer: RQAConfigurationProducer,
) {
    private val log = KotlinLogging.logger {  }

    @RabbitListener(queues = ["\${dqualizer.messaging.queues.rqaDefinitionReceiverQueue.name}"])
    fun receive(@Payload rqaDefinition: RuntimeQualityAnalysisDefinition, @Headers headers: MessageHeaders) {
        log.debug("Received an RQA Definition: {}", rqaDefinition)

        try {
            val rqaConfiguration = translationService.translate(rqaDefinition)

            //publish result
            rqaConfigurationProducer.produce(rqaConfiguration, headers)
        } catch (e: Exception) {
            log.error("Failed to translate RQA Definition: {}", rqaDefinition, e)
            throw AmqpRejectAndDontRequeueException(e)
        }
    }
}
