package de.dqualizer.dqtranslator.messaging

import de.dqualizer.dqtranslator.translation.RQATranslationChain
import de.dqualizer.dqtranslator.translation.RQATranslator
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion
import io.github.dqualizer.dqlang.types.rqa.RQAConfiguration
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class RqaDefinitionReceiver(
    private val rqaTranslators: List<RQATranslator>,
    private val rqaConfigurationProducer: RQAConfigurationProducer,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = ["\${dqualizer.messaging.queues.rqaDefinitionReceiverQueue.name}"])
    fun receive(@Payload rqaDefinition: RuntimeQualityAnalysisDefintion, @Headers headers: MessageHeaders) {
        log.info("Received an RQA Definition")
        val rqaConfiguration = RQATranslationChain()
            .chain(rqaTranslators)
            .chain { _, configuration -> configuration }
            .translate(rqaDefinition)

        rqaConfigurationProducer.produce(rqaConfiguration, headers)
    }
}
