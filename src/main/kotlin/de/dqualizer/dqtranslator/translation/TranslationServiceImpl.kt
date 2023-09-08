package de.dqualizer.dqtranslator.translation

import de.dqualizer.dqtranslator.EnvironmentNotFoundException
import de.dqualizer.dqtranslator.mapping.MappingService
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.Endpoint
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.Artifact
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.loadtest.LoadTestDefinition


import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TranslationServiceImpl(
        private val mappingService: MappingService
) : TranslationService {
    private val log = LoggerFactory.getLogger(TranslationService::class.java)

    override fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition): LoadTestConfiguration {
        // Get Mapping from Api by domain_id
        val mapping = mappingService.getMappingById(rqaDefinition.domainId)

        val loadTestDefinition = rqaDefinition.runtimeQualityAnalysis.loadtests

        // Artifact will always be an Edge...
        val systems = loadTestDefinition.filter { it.artifact.isNode }
        val activities = loadTestDefinition.filter { it.artifact.isEdge }

        log.info(systems.toString())
        log.info(loadTestDefinition.toString())

        val loadTestConfigurations = systems.map { nodeToLoadTests(it, mapping) }.flatten() + activities.map { edgeToLoadTest(it, mapping) }

        val loadtestConfiguration = LoadTestConfiguration(
                rqaDefinition.version,
                rqaDefinition.context,
                rqaDefinition.environment.toString(),
                mapping.getEndpoint(rqaDefinition.environment.toString()),
                loadTestConfigurations.toHashSet()
        )
        log.info(loadtestConfiguration.toString())
        return loadtestConfiguration

    }

    fun nodeToLoadTests(loadtestDefinition: LoadTestDefinition, mapping: DomainArchitectureMapping): List<LoadTestArtifact> {
        return mapping.systems.firstOrNull { it.id == loadtestDefinition.artifact.systemId }
                ?.activities?.map { activity -> activity.endpoint }
                ?.map { LoadTestArtifact(loadtestDefinition.artifact, loadtestDefinition.description, loadtestDefinition.stimulus, loadtestDefinition.responseMeasures, it) }
                ?: throw RuntimeException("Something went very wrong")

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
        return this.activityId != null
    }

    val Artifact.systemId: String
        get() = `systemId`
}