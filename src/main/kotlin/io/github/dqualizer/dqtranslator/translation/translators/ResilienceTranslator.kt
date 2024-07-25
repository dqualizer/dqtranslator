package io.github.dqualizer.dqtranslator.translation.translators

import io.github.dqualizer.dqlang.data.DAMMongoRepository
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.architecture.CodeComponent
import io.github.dqualizer.dqlang.types.dam.architecture.ServiceDescription
import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.CmsbArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.ProcessArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.ResilienceTestArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.ResilienceTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.resilience.ResilienceTestDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.resilience.stimulus.UnavailabilityStimulus
import io.github.dqualizer.dqtranslator.translation.RQATranslator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ResilienceTranslator(
  private val damRepo: DAMMongoRepository
) : RQATranslator {
  private val log = LoggerFactory.getLogger(javaClass)

  override fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition, target: RQAConfiguration): RQAConfiguration {
    target.resilienceConfiguration = translate(rqaDefinition)
    return target
  }

  private fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition): ResilienceTestConfiguration {
    val resilienceTestDefinitions = rqaDefinition.runtimeQualityAnalysis.resilienceTestDefinition
    if (resilienceTestDefinitions.isEmpty()) {
      return ResilienceTestConfiguration()
    }

    val domainArchitectureMapping = damRepo.findById(rqaDefinition.domainId)
      .orElseThrow { NoSuchElementException("No DAM found with id ${rqaDefinition.domainId}") }

    val (resilienceTestDefinitionsForSystems, resilienceTestDefinitionsForActivities) = resilienceTestDefinitions.partition { it.artifact.activityId == null }
    val enrichedResilienceDefinitions = resilienceTestDefinitionsForSystems.map {
      nodeToEnrichedResilienceTestDefinition(
        it,
        domainArchitectureMapping
      )
    } +
      resilienceTestDefinitionsForActivities.map { edgeToResilienceTest(it, domainArchitectureMapping) }

    val resilienceTestConfiguration = ResilienceTestConfiguration(
      rqaDefinition.version,
      rqaDefinition.context,
      rqaDefinition.environment.toString(),
      enrichedResilienceDefinitions.toHashSet()
    )

    log.info(resilienceTestConfiguration.toString())
    return resilienceTestConfiguration
  }

  fun nodeToEnrichedResilienceTestDefinition(
    resilienceTestDefinition: ResilienceTestDefinition,
    mapping: DomainArchitectureMapping
  ): ResilienceTestArtifact {
    val service = getService(resilienceTestDefinition, mapping)

    if (resilienceTestDefinition.stimulus is UnavailabilityStimulus) {
      val enrichedArtifact =
        ProcessArtifact(resilienceTestDefinition.artifact, service.processName!!, service.processPath!!)
      return ResilienceTestArtifact(
        resilienceTestDefinition.name,
        resilienceTestDefinition.description,
        enrichedArtifact,
        null,
        resilienceTestDefinition.stimulus,
        resilienceTestDefinition.responseMeasures
      )
    } else {
      val enrichedArtifact =
        CmsbArtifact(resilienceTestDefinition.artifact, service.cmsbBaseUrl!!, service.packageMember!!)
      return ResilienceTestArtifact(
        resilienceTestDefinition.name,
        resilienceTestDefinition.description,
        null,
        enrichedArtifact,
        resilienceTestDefinition.stimulus,
        resilienceTestDefinition.responseMeasures
      )
    }
  }

  fun edgeToResilienceTest(
    resilienceTestDefinition: ResilienceTestDefinition,
    mapping: DomainArchitectureMapping
  ): ResilienceTestArtifact {
    val service = getService(resilienceTestDefinition, mapping)
    val activityIdFromTestDefinition = resilienceTestDefinition.artifact.activityId

    // use it.targets instead of it.initiators
    // e.g. Petra (initiator) calls endpoint of service (target)
    val activity = mapping.domainStory.activities.first { it.id == activityIdFromTestDefinition }
    val entity = mapping.mapper.mapToArchitecturalEntity(activity) as CodeComponent
    val methodPathForActivity = entity.identifier

    val enrichedArtifact = CmsbArtifact(
      resilienceTestDefinition.artifact,
      service.cmsbBaseUrl!!,
      methodPathForActivity
    )

    return ResilienceTestArtifact(
      resilienceTestDefinition.name,
      resilienceTestDefinition.description,
      null,
      enrichedArtifact,
      resilienceTestDefinition.stimulus,
      resilienceTestDefinition.responseMeasures
    )
  }

  private fun getService(
    resilienceTestDefinition: ResilienceTestDefinition,
    mapping: DomainArchitectureMapping
  ): ServiceDescription {
    // precondition: the selected actor is not empty and mapped to a service
    val actor = mapping.domainStory.actors.firstOrNull { it.id == resilienceTestDefinition.artifact.systemId }
      ?: throw IllegalArgumentException("The selected actor must not be null")
    return mapping.mapper.mapToArchitecturalEntity(actor) as ServiceDescription
  }

}