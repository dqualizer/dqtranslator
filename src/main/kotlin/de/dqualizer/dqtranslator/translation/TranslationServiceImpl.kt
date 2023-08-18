package de.dqualizer.dqtranslator.translation

import de.dqualizer.dqtranslator.EnvironmentNotFoundException
import de.dqualizer.dqtranslator.mapping.MappingService
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.domainarchitecturemapping.Endpoint
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.loadtest.LoadTest
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.loadtest.LoadTestConfig
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.Artifact
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.ModeledLoadTest
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion
import io.github.dqualizer.dqlang.archive.k6adapter.dqlang.loadtest.LoadTestConfig
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TranslationServiceImpl(
        private val mappingService: MappingService
) : TranslationService {
    private val log = LoggerFactory.getLogger(TranslationService::class.java)

    override fun translate(rqaDefinition: RuntimeQualityAnalysisDefintion): LoadTestConfig {
        // Get Mapping from Api by domain_id
        val mapping = mappingService.getMappingByIdrqaDefinition.getDomainId())

        val loadTestSpecs = rqaDefinition.rqa.loadtests
        val nodes = loadTestSpecs.filter { it.artifact.isNode() }
        val edges = loadTestSpecs.filter { it.artifact.isEdge() }

        val loadTestConfigurations = nodes.map { nodeToLoadTests(it, mapping) }.flatten() + edges.map { edgeToLoadTest(it, mapping) }

        return LoadTestConfig(
                rqaDefinition.version,
                rqaDefinition.context,
                rqaDefinition.environment,
                mapping.getEndpoint(rqaDefinition.environment),
                LinkedHashSet(loadTestConfigurations))
    }

    fun nodeToLoadTests(loadtestSpec: ModeledLoadTest, mapping: DomainArchitectureMapping): List<LoadTest> {
        return mapping.objects.firstOrNull { it.dqID == loadtestSpec.artifact.`object` }
                ?.activities?.map { activity -> activity.endpoint }?.map { LoadTest(loadtestSpec.artifact, loadtestSpec.description, loadtestSpec.stimulus, loadtestSpec.responseMeasure, it) }
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