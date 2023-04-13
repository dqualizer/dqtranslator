package de.dqualizer.dqtranslator.messaging

import de.dqualizer.dqtranslator.translation.TranslationService
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class RqaDefinitionReceiver(
        private val translationService: TranslationService,
        private val loadTestConfigurationClient: TestConfigurationClient
) {
    private val log = LoggerFactory.getLogger(RqaDefinitionReceiver::class.java)

    @RabbitListener(queues = ["\${dqualizer.rabbitmq.rqaDefinitionQueue}"])
    fun receive(@Payload rqaDefinition: RuntimeQualityAnalysisDefintion) {
        log.info("RqaDefinitionReceiver received $rqaDefinition")

        val loadTestConfig = translationService.translate(rqaDefinition)
        loadTestConfigurationClient.queueLoadTestConfiguration(loadTestConfig)
    }
}