package io.github.dqualizer.dqtranslator.translation.translators

import io.github.dqualizer.dqlang.draft.rqaDefinition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.Endpoint
import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTest
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.Artifact
import io.github.dqualizer.dqlang.types.rqa.definition.loadtest.ModeledLoadTest
import io.github.dqualizer.dqtranslator.EnvironmentNotFoundException
import io.github.dqualizer.dqtranslator.mapping.MappingService
import io.github.dqualizer.dqtranslator.translation.RQATranslator
import io.github.dqualizer.dqtranslator.translation.TranslationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LoadTestTranslator(
    private val mappingService: MappingService
) : RQATranslator {
    private val log = LoggerFactory.getLogger(TranslationService::class.java)

    override fun translate(rqaDef: RuntimeQualityAnalysisDefinition, rqaConfig: RQAConfiguration): RQAConfiguration {
        if (rqaDef.runtimeQualityAnalysis.loadtests.size <= 0) {
            log.debug("No loadtests found in RQA Definition")
            return rqaConfig
        }

        val mapping = mappingService.getDAMByContext(rqaDef.domainId)

        val loadTestSpecs = rqaDef.runtimeQualityAnalysis.loadtests
        val nodes = loadTestSpecs.filter { it.artifact.isNode }
        val edges = loadTestSpecs.filter { it.artifact.isEdge }

        val loadTestConfigurations =
            nodes.map { nodeToLoadTests(it, mapping) }.flatten() + edges.map { edgeToLoadTest(it, mapping) }

        rqaConfig.loadConfiguration = LoadTestConfiguration(
            rqaDef.version,
            rqaDef.domainId,
            rqaDef.environment.toString(),
            mapping.getEndpoint(rqaDef.environment.toString()),
            loadTestConfigurations.toMutableSet()
        )

        return rqaConfig
    }

    fun nodeToLoadTests(loadtestModel: ModeledLoadTest, mapping: DomainArchitectureMapping): List<LoadTest> {
        return mapping.systems.firstOrNull { it.id == loadtestModel.artifact.systemId }
            ?.activities?.map { activity -> activity.endpoint }?.map {
                LoadTest(
                    loadtestModel.artifact,
                    loadtestModel.description,
                    loadtestModel.stimulus,
                    loadtestModel.responseMeasures,
                    it
                )
            }
            ?: throw RuntimeException("Something went very wrong")
    }

    fun edgeToLoadTest(loadtestModel: ModeledLoadTest, mapping: DomainArchitectureMapping): LoadTest {
        return LoadTest(
            loadtestModel.artifact,
            loadtestModel.description,
            loadtestModel.stimulus,
            loadtestModel.responseMeasures,
            loadtestModel.getEndpoint(mapping)
        )
    }

    private fun ModeledLoadTest.getEndpoint(mapping: DomainArchitectureMapping): Endpoint {
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
}
