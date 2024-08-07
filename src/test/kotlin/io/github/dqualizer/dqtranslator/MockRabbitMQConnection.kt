package io.github.dqualizer.dqtranslator

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MockRabbitMQConnection {
  @Bean
  fun connectionFactory(): ConnectionFactory {
    return CachingConnectionFactory(MockConnectionFactory())
  }
}
