package de.dqualizer.dqtranslator.api


import com.fasterxml.jackson.databind.ObjectMapper
import de.dqualizer.dqtranslator.messaging.RQAConfigurationProducer
import de.dqualizer.dqtranslator.translation.TranslationService
import io.github.dqualizer.dqlang.types.dam.Payload
import io.github.dqualizer.dqlang.types.dam.Scenario
import io.github.dqualizer.dqlang.types.rqa.definition.Artifact
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysis
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.enums.*
import io.github.dqualizer.dqlang.types.rqa.definition.loadtest.LoadTestDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.loadtest.Parametrization
import io.github.dqualizer.dqlang.types.rqa.definition.loadtest.ResponseMeasures
import io.github.dqualizer.dqlang.types.rqa.definition.loadtest.stimulus.ConstantLoadStimulus
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.ResilienceResponseMeasures
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.ResilienceTestDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.stimulus.UnavailabilityStimulus
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import kotlin.io.path.Path


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
            val rqaDefinition = objectMapper.readValue(rqaResponse.bodyAsText(), RuntimeQualityAnalysisDefinition::class.java)

            if(rqaDefinition.runtimeQualityAnalysis.resilienceTests.isNotEmpty()){
                val resilienceTestConfig = translationService.translateRqaDefToResilienceTestConfig(rqaDefinition)
                log.info(resilienceTestConfig.enrichedResilienceTestDefinitions.toString())
                rqaConfigurationProducer.produceResilienceTestConfig(resilienceTestConfig)
            }

            if (rqaDefinition.runtimeQualityAnalysis.loadtests.isNotEmpty()){
                val loadTestConfig = translationService.translateRqaDefToLoadTestConfig(rqaDefinition)
                log.info(loadTestConfig.loadTestArtifacts.toString())
                rqaConfigurationProducer.produceLoadtestConfig(loadTestConfig)
            }
        }
    }
}