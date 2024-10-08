package io.github.dqualizer.dqtranslator.translation.translators

import io.github.dqualizer.dqlang.data.DAMMongoRepository
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.architecture.RESTEndpoint
import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.loadtest.LoadTestDefinition
import io.github.dqualizer.dqtranslator.EnvironmentNotFoundException
import io.github.dqualizer.dqtranslator.translation.RQATranslator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LoadTranslator(
  val damRepo: DAMMongoRepository
) : RQATranslator {
  private val log = LoggerFactory.getLogger(javaClass)

  override fun translate(
    rqaDefinition: RuntimeQualityAnalysisDefinition,
    target: RQAConfiguration
  ): RQAConfiguration {
    target.loadConfiguration = translate(rqaDefinition)
    return target
  }

  private fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition): LoadTestConfiguration {
    val dam = damRepo.findById(rqaDefinition.domainId)
      .orElseThrow { NoSuchElementException("No DAM found with id ${rqaDefinition.domainId}") }

    val loadTestDefinition = rqaDefinition.runtimeQualityAnalysis.loadTestDefinition
    if (loadTestDefinition.isEmpty()) {
      return LoadTestConfiguration()
    }

    // Artifact will always be an Edge...
    val (systems, activities) = loadTestDefinition.partition { it.artifact?.activityId == null }

    val loadTestConfigurations =
      systems.map { nodeToLoadTests(it, dam) }.flatten() + activities.map { edgeToLoadTest(it, dam) }

    val loadtestConfiguration = LoadTestConfiguration(
      rqaDefinition.version,
      rqaDefinition.context,
      rqaDefinition.environment.toString(),
      dam.getEndpoint(rqaDefinition.environment.toString()),
      loadTestConfigurations.toHashSet()
    )
    log.info(loadtestConfiguration.toString())
    return loadtestConfiguration
  }

  fun nodeToLoadTests(
    loadtestDefinition: LoadTestDefinition,
    mapping: DomainArchitectureMapping
  ): List<LoadTestArtifact> {
    val actor = mapping.domainStory.actors.firstOrNull { it.id == loadtestDefinition.artifact?.systemId }
    val activitiesOfSpecifiedActor = mapping.domainStory.activities.filter { it.initiators.contains(actor?.id) }
    val loadTestArtifacts = mutableListOf<LoadTestArtifact>()
    for (activity in activitiesOfSpecifiedActor) {
      // Not tested
      val endpoint = mapping.mapper.mapToArchitecturalEntity(activity) as RESTEndpoint
      loadTestArtifacts.add(
        LoadTestArtifact(
          loadtestDefinition.artifact,
          loadtestDefinition.description,
          loadtestDefinition.stimulus,
          loadtestDefinition.responseMeasures,
          endpoint
        )
      )
    }
    return loadTestArtifacts
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

  private fun LoadTestDefinition.getEndpoint(mapping: DomainArchitectureMapping): RESTEndpoint {
    val actor = mapping.domainStory.actors.first { it.id == this.artifact?.systemId }

    // use it.targets instead of it.initiators
    // e.g. Petra (initiator) calls endpoint of service (target)
    val activityOfSpecifiedActor = mapping.domainStory.activities.first { it.targets.contains(actor.id) }
    val entity = mapping.mapper.mapToArchitecturalEntity(activityOfSpecifiedActor)

    return mapping.softwareSystem.endpoints.first { it.codeComponent == entity.id }
  }

  private fun DomainArchitectureMapping.getEndpoint(environment: String): String {
    val services = this.softwareSystem.services
    for (service in services) {
      val serviceInfos = service.apiSchema!!.serverInfo
      for (serviceInfo in serviceInfos) {
        if (serviceInfo.environment == environment) {
          return serviceInfo.host!!
        }
      }
    }
    throw EnvironmentNotFoundException(environment)
  }
}
