package de.dqualizer.dqtranslator.messaging

import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TestConfigurationClientImpl(
        private val rabbitTemplate: RabbitTemplate
) : TestConfigurationClient {
    @Value("\${dqualizer.rabbitmq.loadtestExchange}")
    private lateinit var loadTestExchangeName: String

    private val log = LoggerFactory.getLogger(TestConfigurationClient::class.java)

    override fun queueLoadTestConfiguration(loadTestConfiguration: LoadTestConfiguration) {
        rabbitTemplate.convertAndSend(
                loadTestExchangeName,
                "GET",
                loadTestConfiguration
        )
        log.info("Sent loadTestConfiguration=$loadTestConfiguration to queue")
    }
}