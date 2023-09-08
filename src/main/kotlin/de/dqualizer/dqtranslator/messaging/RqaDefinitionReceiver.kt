package de.dqualizer.dqtranslator.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import de.dqualizer.dqtranslator.translation.TranslationService
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class RqaDefinitionReceiver(
        private val translationService: TranslationService,
        private val loadTestConfigurationClient: TestConfigurationClient,
        private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(RqaDefinitionReceiver::class.java)

    @RabbitListener(queues = ["\${dqualizer.rabbitmq.rqaDefinitionQueue}"])
    fun receive(@Payload rqaDefinition: String) {
        log.info("RqaDefinitionReceiver received $rqaDefinition")
        val rqaDef = objectMapper.readValue(rqaDefinition, RuntimeQualityAnalysisDefinition::class.java)
        val loadTestConfig = translationService.translate(rqaDef)
        loadTestConfigurationClient.queueLoadTestConfiguration(loadTestConfig)
    }
}