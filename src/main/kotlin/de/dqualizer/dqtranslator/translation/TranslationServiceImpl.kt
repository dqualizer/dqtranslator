package de.dqualizer.dqtranslator.translation

import de.dqualizer.dqtranslator.EnvironmentNotFoundException
import de.dqualizer.dqtranslator.mapping.MappingService
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.Endpoint
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.EnrichedArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.EnrichedResilienceTestDefinition
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.ResilienceTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.Artifact
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.loadtest.LoadTestDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.ResilienceTestDefinition


import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TranslationServiceImpl(
    private val mappingService: MappingService
) : TranslationService {
    private val log = LoggerFactory.getLogger(TranslationService::class.java)

    override fun translateRqaDefToLoadTestConfig(rqaDefinition: RuntimeQualityAnalysisDefinition): LoadTestConfiguration {
        // Get domain architecture Mapping from Api by domain_id
        val domainArchitectureMapping = mappingService.getMappingById(rqaDefinition.domainId)
        val loadTestDefinition = rqaDefinition.runtimeQualityAnalysis.loadtests

        // Artifact will always be an Edge...
        val (loadTestsForSystems, loadTestsForActivities) = loadTestDefinition.partition { it.artifact.activityId == null }
        val loadTestConfigurations = loadTestsForSystems.map { nodeToLoadTest(it, domainArchitectureMapping) }.flatten() + loadTestsForActivities.map { edgeToLoadTest(it, domainArchitectureMapping) }

        loadTestConfigurations.forEach {loadTestConfiguration ->
            loadTestConfiguration.endpoint.payloads = ArrayList()
            }

        val loadtestConfiguration = LoadTestConfiguration(
                rqaDefinition.version,
                rqaDefinition.context,
                rqaDefinition.environment.toString(),
                domainArchitectureMapping.getEndpoint(rqaDefinition.environment.toString()),
                loadTestConfigurations.toHashSet()
        )

        log.info(loadtestConfiguration.toString())
        return loadtestConfiguration;

    }


    override fun translateRqaDefToResilienceTestConfig(rqaDefinition: RuntimeQualityAnalysisDefinition): ResilienceTestConfiguration {
        // Get domain architecture Mapping from Api by domain_id
        val domainArchitectureMapping = mappingService.getMappingById(rqaDefinition.domainId)

        val resilienceTestDefinitions = rqaDefinition.runtimeQualityAnalysis.resilienceTests
        val (resilienceTestDefinitionsForSystems, resilienceTestDefinitionsForActivities) = resilienceTestDefinitions.partition { it.artifact.activityId == null }
        // TODO translate Resilience Tests for activities
        val enrichedResilienceDefinitions = resilienceTestDefinitionsForSystems.map { nodeToEnrichedResilienceTestDefinition(it, domainArchitectureMapping) }// + resilienceTestDefinitionsForActivities.map { edgeToResilienceTest(it, domainArchitectureMapping) }

        val resilienceTestConfiguration = ResilienceTestConfiguration(
                rqaDefinition.version,
                rqaDefinition.context,
                rqaDefinition.environment.toString(),
                enrichedResilienceDefinitions.toHashSet()
        )

        log.info(resilienceTestConfiguration.toString())
        return resilienceTestConfiguration;
    }

    /*
    * When we define load test on a system, multiple activities/endpoints can be affected, thats why we create a List of LoadTestArtifcats
    * */
    fun nodeToLoadTest(loadtestDefinition: LoadTestDefinition, mapping: DomainArchitectureMapping): List<LoadTestArtifact> {
        return mapping.systems.firstOrNull { it.id == loadtestDefinition.artifact.systemId }
                ?.activities?.map { activity -> activity.endpoint }
                ?.map { LoadTestArtifact(loadtestDefinition.artifact, loadtestDefinition.description, loadtestDefinition.stimulus, loadtestDefinition.responseMeasures, it) }
                ?: throw RuntimeException("Something went very wrong")

    }

    fun nodeToEnrichedResilienceTestDefinition(resilienceTestDefinition: ResilienceTestDefinition, mapping: DomainArchitectureMapping): EnrichedResilienceTestDefinition {
        val system = mapping.systems.firstOrNull { it.id == resilienceTestDefinition.artifact.systemId}
        if (system?.type?.equals("Process") == true || system?.type?.equals("Class") == true){
            val enrichedArtifact: EnrichedArtifact = EnrichedArtifact(resilienceTestDefinition.artifact, system!!.processId, system!!.processPath,  system!!.baseUrl, system!!.packageMember)
            return EnrichedResilienceTestDefinition(enrichedArtifact, resilienceTestDefinition.description, resilienceTestDefinition.stimulus, resilienceTestDefinition.responseMeasures)
        }

        throw RuntimeException("Something went very wrong")
    }

    fun edgeToLoadTest(loadtestSpec: LoadTestDefinition, mapping: DomainArchitectureMapping): LoadTestArtifact {
        return LoadTestArtifact(
                loadtestSpec.artifact,
                loadtestSpec.description,
                loadtestSpec.stimulus,
                loadtestSpec.responseMeasures,
                loadtestSpec.getEndpoint(mapping)
                )
    }

    /*fun edgeToResilienceTest(resilienceTestDefinition: ResilienceTestDefinition, mapping: DomainArchitectureMapping): EnrichedResilienceTestDefinition {
        return EnrichedResilienceTestDefinition(
                resilienceTestDefinition.artifact,
                resilienceTestDefinition.description,
                resilienceTestDefinition.stimulus,
                resilienceTestDefinition.responseMeasures,
                resilienceTestDefinition.getEndpoint(mapping)
        )
    }*/

    private fun LoadTestDefinition.getEndpoint(mapping: DomainArchitectureMapping): Endpoint {
        return mapping.systems.firstOrNull { it.id == this.artifact.systemId }
                ?.activities?.firstOrNull { it.id == this.artifact.activityId }
                ?.endpoint
                ?: throw RuntimeException("Something went very wrong")
    }

    private fun DomainArchitectureMapping.getEndpoint(environment: String): String {
        return this.serverInfos.firstOrNull { it.environment == environment }?.host
                ?: throw EnvironmentNotFoundException(environment)
    }

    private fun Artifact.isNode(): Boolean {
        return this.activityId == null
    }

    private fun Artifact.isEdge(): Boolean {
        return this.systemId != null
    }

    val Artifact.systemId: String
        get() = `systemId`
}