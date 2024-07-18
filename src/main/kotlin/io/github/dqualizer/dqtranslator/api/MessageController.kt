package io.github.dqualizer.dqtranslator.api


import com.fasterxml.jackson.databind.ObjectMapper
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqtranslator.messaging.RQAConfigurationProducer
import io.github.dqualizer.dqtranslator.translation.RQATranslationChain
import io.github.dqualizer.dqtranslator.translation.RQATranslator
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
class MessageController(
  private val rqaTranslators: List<RQATranslator>,
  private val rqaConfigurationProducer: RQAConfigurationProducer,
  private val objectMapper: ObjectMapper
) {
  private val log = LoggerFactory.getLogger(MessageController::class.java)

  val client = HttpClient {
    install(Logging)
  }

  @Value("\${dqualizer.dqapi.host}")
  private lateinit var dqApiHost: String

  @Value("\${dqualizer.dqapi.port}")
  private lateinit var dqApiPort: String

  @PostMapping("/translate/{rqaId}")
  fun index(@PathVariable rqaId: String, @Headers headers: MessageHeaders) {
    log.info("RqaDefinitionReceiver received $rqaId. Searching for the rqa-def now...")
    val rqaResponse: HttpResponse

    runBlocking {
      rqaResponse = client.get("http://${dqApiHost}:${dqApiPort}/api/v2/rqa/$rqaId")
      log.info("RQA response: $rqaResponse")
      val rqaDefinition = objectMapper.readValue(rqaResponse.bodyAsText(), RuntimeQualityAnalysisDefinition::class.java)

      val rqaConfiguration = RQATranslationChain()
        .chain(rqaTranslators)
        .translate(rqaDefinition)

      log.info(rqaConfiguration.loadConfiguration.toString())
      rqaConfigurationProducer.produce(rqaConfiguration, headers)
    }
  }
}
