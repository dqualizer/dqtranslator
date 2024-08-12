package io.github.dqualizer.dqtranslator.translation.translators

import io.github.dqualizer.dqlang.data.DAMMongoRepository
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.architecture.ArchitectureEntity
import io.github.dqualizer.dqlang.types.dam.architecture.CodeComponent
import io.github.dqualizer.dqlang.types.dam.architecture.ServiceDescription
import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.CmsbArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.ProcessArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.ResilienceTestArtifact
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.ResilienceTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.Artifact
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
      nodeToResilienceTest(
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

  private fun toTechnicalArtifact(service: ServiceDescription?, method: CodeComponent?): Artifact {
    return Artifact(service?.id, method?.id)
  }

  /**
   * create test for actor
   */
  fun nodeToResilienceTest(
    resilienceTestDefinition: ResilienceTestDefinition,
    mapping: DomainArchitectureMapping
  ): ResilienceTestArtifact {
    val entity = getEntity(resilienceTestDefinition, mapping)
    val service = getService(entity, mapping)

    if (resilienceTestDefinition.stimulus is UnavailabilityStimulus) {
      val enrichedArtifact =
        ProcessArtifact(toTechnicalArtifact(service, null), service.processName!!, service.processPath!!)
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

  /**
   * create test for activity
   */
  fun edgeToResilienceTest(
    resilienceTestDefinition: ResilienceTestDefinition,
    mapping: DomainArchitectureMapping
  ): ResilienceTestArtifact {
    val entity = getEntity(resilienceTestDefinition, mapping)
    val service = getService(entity, mapping)
    val activityIdFromTestDefinition = resilienceTestDefinition.artifact.activityId

    // use it.targets instead of it.initiators
    // e.g. Petra (initiator) calls endpoint of service (target)
    val activity = mapping.domainStory.activities.first { it.id == activityIdFromTestDefinition }
    val method = mapping.mapper.mapToArchitecturalEntity(activity) as CodeComponent
    val methodPathForActivity = method.identifier

    if (resilienceTestDefinition.stimulus is UnavailabilityStimulus) {
      val enrichedArtifact =
        ProcessArtifact(toTechnicalArtifact(service, method), service.processName!!, service.processPath!!)

      return ResilienceTestArtifact(
        resilienceTestDefinition.name,
        resilienceTestDefinition.description,
        enrichedArtifact,
        null,
        resilienceTestDefinition.stimulus,
        resilienceTestDefinition.responseMeasures
      )
    }

    val enrichedArtifact = CmsbArtifact(
      toTechnicalArtifact(service, method),
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

  /**
   * Returns the technical entity specified in the resilienceTestDefinition
   */
  private fun getEntity(
    resilienceTestDefinition: ResilienceTestDefinition,
    mapping: DomainArchitectureMapping
  ): ArchitectureEntity {
    // precondition: the selected actor is not empty and mapped to a service
    val actor = mapping.domainStory.actors.firstOrNull { it.id == resilienceTestDefinition.artifact.systemId }
      ?: throw IllegalArgumentException("The selected actor must not be null")
    return mapping.mapper.mapToArchitecturalEntity(actor)
  }

  /**
   * Return ServiceDescription containing the provided entity
   */
  private fun getService(
    entity: ArchitectureEntity,
    mapping: DomainArchitectureMapping): ServiceDescription {
    return mapping.softwareSystem.services.firstOrNull { it.codeComponents.contains(entity) }
      ?: throw NoSuchElementException("There is no service with id ${entity.id}")
  }
}