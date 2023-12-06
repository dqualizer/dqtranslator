package de.dqualizer.dqtranslator.api

import com.fasterxml.jackson.databind.ObjectMapper
import de.dqualizer.dqtranslator.messaging.RQAConfigurationProducer
import de.dqualizer.dqtranslator.translation.TranslationService
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition


import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value


@RestController
@CrossOrigin(origins = ["*"])
class MessageController (private val translationService: TranslationService,
                         private val rqaConfigurationProducer: RQAConfigurationProducer,
                         private val objectMapper: ObjectMapper) {
    private val log = LoggerFactory.getLogger(MessageController::class.java)

    val client = HttpClient {
        install(Logging)
    };

    @Value("\${dqualizer.dqapi.host}")
    private lateinit var dqApiHost : String;
    @Value("\${dqualizer.dqapi.port}")
    private lateinit var dqApiPort : String;

    @PostMapping("/translate/{rqaId}")
    fun index(@PathVariable rqaId: String) {
        log.info("RqaDefinitionReceiver received $rqaId. Searching for the rqa-def now...")
        val rqaResponse : HttpResponse

        runBlocking {
            rqaResponse = client.get("http://${dqApiHost}:${dqApiPort}/api/v1/rqa-definition/$rqaId")
            val rqaDef = objectMapper.readValue(rqaResponse.bodyAsText(), RuntimeQualityAnalysisDefinition::class.java)
            val loadTestConfig = translationService.translateRqaDefToLoadTestConfig(rqaDef)
            val resilienceTestConfig = translationService.translateRqaDefToResilienceTestConfig(rqaDef)
            log.info(loadTestConfig.loadTestArtifacts.toString())
            log.info(resilienceTestConfig.enrichedResilienceTestDefinitions.toString())
            rqaConfigurationProducer.produce(loadTestConfig)
           // rqaConfigurationProducer.produce(resilienceTestConfig)

        }



    }
}