package io.github.dqualizer.dqtranslator.translators.resilience

import io.github.dqualizer.dqlang.data.DAMMongoRepository
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.architecture.CodeComponent
import io.github.dqualizer.dqlang.types.dam.architecture.ServiceDescription
import io.github.dqualizer.dqlang.types.dam.architecture.SoftwareSystem
import io.github.dqualizer.dqlang.types.dam.domainstory.DomainStory
import io.github.dqualizer.dqlang.types.dam.domainstory.System
import io.github.dqualizer.dqlang.types.dam.mapping.SystemToComponentMapping
import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.Artifact
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysis
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.enums.Environment
import io.github.dqualizer.dqlang.types.rqa.definition.enums.Satisfaction
import io.github.dqualizer.dqlang.types.rqa.definition.resilience.ResilienceResponseMeasures
import io.github.dqualizer.dqlang.types.rqa.definition.resilience.ResilienceTestDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.resilience.stimulus.UnavailabilityStimulus
import io.github.dqualizer.dqtranslator.translation.translators.ResilienceTranslator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import java.util.*
import java.util.Collections.singletonList

class ResilienceTranslatorsTest {

  private var mappingService = Mockito.mock(DAMMongoRepository::class.java)
  private var translationServiceImplForTest = ResilienceTranslator(mappingService)

  @Test
  fun testTranslateRqaDefToResilienceTestConfig() {
    //arrange
    val systemId = "systemTestId"
    val domainId = "testDomainId"
    val processName = "aTestingProcessName"
    val componentId = "componentTestId"

    val system = System("testSystem")
    system.id = domainId

    val targetComponent = Mockito.mock(CodeComponent::class.java)
    whenever(targetComponent.id).thenReturn(componentId)
    val targetService = Mockito.mock(ServiceDescription::class.java)
    whenever(targetService.name).thenReturn("someServiceName")
    whenever(targetService.codeComponents).thenReturn(singletonList(targetComponent))
    whenever(targetService.id).thenReturn(systemId)
    whenever(targetService.processName).thenReturn(processName)
    whenever(targetService.processPath).thenReturn("aTestingProcessPath")
    val softwareSystem = SoftwareSystem("testSystem", Environment.DEV, listOf(targetService))

    val domainStory = DomainStory(listOf(system), emptyList(), emptyList())

    val mapping = SystemToComponentMapping(domainId, componentId)
    val dam = DomainArchitectureMapping(softwareSystem, domainStory, setOf(mapping))
    whenever(mappingService.findById(domainId)).thenReturn(Optional.of(dam))

    val artifact = Artifact(domainId, null)
    val stimulus = UnavailabilityStimulus(5, 10)
    val responseMeasures = ResilienceResponseMeasures(Satisfaction.TOLERATED, null, null)
    val resilienceTestDefinition =
      ResilienceTestDefinition("TestDefinition", artifact, "TestDescription", stimulus, responseMeasures)
    val runtimeQualityAnalysis = RuntimeQualityAnalysis()
    runtimeQualityAnalysis.resilienceTestDefinition.add(resilienceTestDefinition)
    val rqaDefinition = RuntimeQualityAnalysisDefinition(
      "testName",
      "1",
      domainId,
      "testContext",
      Environment.DEV,
      runtimeQualityAnalysis
    )

    //act
    val rqaConfiguration = RQAConfiguration("test")
    val result = translationServiceImplForTest.translate(rqaDefinition, rqaConfiguration).resilienceConfiguration

    //assert
    assertAll(
      { Assertions.assertEquals("1", result.version) },
      { Assertions.assertEquals("testContext", result.context) },
      { Assertions.assertEquals("DEV", result.environment) },
      { Assertions.assertEquals(processName, result.resilienceTestArtifacts.first().processArtifact!!.processName) },
      { Assertions.assertEquals(systemId, result.resilienceTestArtifacts.first().processArtifact!!.artifact.systemId) },
      { Assertions.assertEquals(null, result.resilienceTestArtifacts.first().processArtifact!!.artifact.activityId) },
      { Assertions.assertEquals("TestDescription", result.resilienceTestArtifacts.first().description) },
      { Assertions.assertTrue(result.resilienceTestArtifacts.first().stimulus is UnavailabilityStimulus) },
      { Assertions.assertEquals(5, result.resilienceTestArtifacts.first().stimulus!!.pauseBeforeTriggeringSeconds) },
      { Assertions.assertEquals(10, result.resilienceTestArtifacts.first().stimulus!!.experimentDurationSeconds) },
      {
        Assertions.assertEquals(
          Satisfaction.TOLERATED,
          result.resilienceTestArtifacts.first().responseMeasure!!.recoveryTime
        )
      },
    )
  }

}