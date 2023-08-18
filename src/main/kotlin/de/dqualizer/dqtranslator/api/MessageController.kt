package de.dqualizer.dqtranslator.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature
import de.dqualizer.dqtranslator.messaging.RqaDefinitionReceiver
import de.dqualizer.dqtranslator.messaging.TestConfigurationClient
import de.dqualizer.dqtranslator.translation.TranslationService
import io.github.dqualizer.dqlang.draft.rqa.RqaDefinition
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.Identity.decode
import io.ktor.util.Identity.encode
import kotlinx.coroutines.runBlocking


@RestController
@CrossOrigin(origins = ["*"])
class MessageController (private val translationService: TranslationService,
                         private val loadTestConfigurationClient: TestConfigurationClient,
                         private val objectMapper: ObjectMapper) {
    private val log = LoggerFactory.getLogger(RqaDefinitionReceiver::class.java)

    val client = HttpClient {
        install(Logging)
    };

    @PostMapping("/translate/{rqaId}")
    fun index(@PathVariable rqaId: String) {
        log.info("RqaDefinitionReceiver received $rqaId. Searching for the rqa-def now...")
        val rqaResponse : HttpResponse

        runBlocking {
            rqaResponse = client.get("http://localhost:8099/api/v1/rqa-definition/$rqaId")
            val rqaDef = objectMapper.readValue(rqaResponse.bodyAsText(), RqaDefinition::class.java)
            val loadTestConfig = translationService.translate(rqaDef)
        }



    }
}