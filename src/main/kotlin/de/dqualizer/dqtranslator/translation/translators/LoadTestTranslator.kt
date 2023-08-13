package de.dqualizer.dqtranslator.translation.translators

import de.dqualizer.dqtranslator.EnvironmentNotFoundException
import de.dqualizer.dqtranslator.mapping.MappingService
import de.dqualizer.dqtranslator.translation.RQATranslator
import de.dqualizer.dqtranslator.translation.TranslationService
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.domainarchitecturemapping.DomainArchitectureMapping
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.domainarchitecturemapping.Endpoint
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.loadtest.LoadTest
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.Artifact
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.ModeledLoadTest
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion
import io.github.dqualizer.dqlang.types.load_test_configuration.LoadTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.RQAConfiguration
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LoadTestTranslator(
    private val mappingService: MappingService
) : RQATranslator {
    private val log = LoggerFactory.getLogger(TranslationService::class.java)

    override fun translate(rqaDefinition: RuntimeQualityAnalysisDefintion, target: RQAConfiguration): RQAConfiguration {
        val mapping = mappingService.getMappingByContext(rqaDefinition.context)

        val loadTestSpecs = rqaDefinition.rqa.loadtests
        val nodes = loadTestSpecs.filter { it.artifact.isNode() }
        val edges = loadTestSpecs.filter { it.artifact.isEdge() }

        val loadTestConfigurations =
            nodes.map { nodeToLoadTests(it, mapping) }.flatten() + edges.map { edgeToLoadTest(it, mapping) }

        target.loadConfiguration = LoadTestConfiguration(
            rqaDefinition.version,
            rqaDefinition.context,
            rqaDefinition.environment,
            mapping.getEndpoint(rqaDefinition.environment),
            loadTestConfigurations.toMutableSet()
        )

        return target
    }

    fun nodeToLoadTests(loadtestSpec: ModeledLoadTest, mapping: DomainArchitectureMapping): List<LoadTest> {
        return mapping.objects.firstOrNull { it.dqID == loadtestSpec.artifact.`object` }
            ?.activities?.map { activity -> activity.endpoint }?.map {
                LoadTest(
                    loadtestSpec.artifact,
                    loadtestSpec.description,
                    loadtestSpec.stimulus,
                    loadtestSpec.responseMeasure,
                    it
                )
            }
            ?: throw RuntimeException("Something went very wrong")
    }

    fun edgeToLoadTest(loadtestSpec: ModeledLoadTest, mapping: DomainArchitectureMapping): LoadTest {
        return LoadTest(
            loadtestSpec.artifact,
            loadtestSpec.description,
            loadtestSpec.stimulus,
            loadtestSpec.responseMeasure,
            loadtestSpec.getEndpoint(mapping)
        )
    }

    private fun ModeledLoadTest.getEndpoint(mapping: DomainArchitectureMapping): Endpoint {
        return mapping.objects.firstOrNull { it.dqID == this.artifact.objectId }
            ?.activities?.firstOrNull { it.dqID == this.artifact.activity }
            ?.endpoint
            ?: throw RuntimeException("Something went very wrong")
    }

    private fun DomainArchitectureMapping.getEndpoint(environment: String): String {
        return this.serverInfo.firstOrNull { it.environment == environment }?.host
            ?: throw EnvironmentNotFoundException(environment)
    }

    private fun Artifact.isNode(): Boolean {
        return this.activity == null
    }

    private fun Artifact.isEdge(): Boolean {
        return this.activity != null
    }

    val Artifact.objectId: String
        get() = `object`
}
