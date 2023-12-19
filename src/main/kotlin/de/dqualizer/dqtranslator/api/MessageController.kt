package de.dqualizer.dqtranslator.api

import com.fasterxml.jackson.databind.ObjectMapper
import de.dqualizer.dqtranslator.messaging.RQAConfigurationProducer
import de.dqualizer.dqtranslator.translation.TranslationService
import io.github.dqualizer.dqlang.types.rqa.definition.Artifact
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysis
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.enums.Environment
import io.github.dqualizer.dqlang.types.rqa.definition.enums.Satisfaction
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.ResilienceResponseMeasures
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.ResilienceTestDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.stimulus.UnavailabilityStimulus


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
            rqaConfigurationProducer.produceLoadtestConfig(loadTestConfig)
           // rqaConfigurationProducer.produce(resilienceTestConfig)

        }
    }

    @PostMapping("/translate/hardcoded")
    fun translateHardCodedRqaDefinition() {
        log.info("RqaDefinitionReceiver received order to translate hardcoded RQA Definition. Starting now...")

        val artifact = Artifact("MyComputer", null)
        val stimulus = UnavailabilityStimulus("Unavailability", 100)
        val responseMeasures = ResilienceResponseMeasures(Satisfaction.TOLERATED)
        val resilienceTestDefinition = ResilienceTestDefinition("TestDefinition", artifact, "TestDescription", stimulus, responseMeasures)
        val runtimeQualityAnalysis = RuntimeQualityAnalysis()
        runtimeQualityAnalysis.resilienceTests.add(resilienceTestDefinition)
        val rqaDefinition = RuntimeQualityAnalysisDefinition("A testing rqaDef", "1", Environment.DEV, "Computer work station", "Using a password manager", runtimeQualityAnalysis)

        runBlocking {

            val loadTestConfig = translationService.translateRqaDefToLoadTestConfig(rqaDefinition)
            val resilienceTestConfig = translationService.translateRqaDefToResilienceTestConfig(rqaDefinition)
            log.info(loadTestConfig.loadTestArtifacts.toString())
            log.info(resilienceTestConfig.enrichedResilienceTestDefinitions.toString())
            rqaConfigurationProducer.produceLoadtestConfig(loadTestConfig)
            rqaConfigurationProducer.produceResilienceTestConfig(resilienceTestConfig)

        }
    }
}