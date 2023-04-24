package de.dqualizer.dqtranslator.messaging

import dqualizer.dqlang.archive.loadtesttranslator.dqlang.loadtest.LoadTestConfig
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

    override fun queueLoadTestConfiguration(loadTestConfiguration: LoadTestConfig) {
        rabbitTemplate.convertAndSend(
                loadTestExchangeName,
                "post",
                loadTestConfiguration
        )
        log.info("Sent loadTestConfiguration=$loadTestConfiguration to queue")
    }
}