package de.dqualizer.dqtranslator

import de.dqualizer.dqtranslator.mapping.MappingServiceImpl
import de.dqualizer.dqtranslator.translation.TranslationServiceImpl
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.System
import io.github.dqualizer.dqlang.types.rqa.definition.Artifact
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysis
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.enums.Environment
import io.github.dqualizer.dqlang.types.rqa.definition.enums.Satisfaction
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.ResilienceResponseMeasures
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.ResilienceTestDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.resiliencetest.stimulus.UnavailabilityStimulus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito
import org.mockito.kotlin.whenever

class TranslationServiceImplTest {

    private var mappingService = Mockito.mock(MappingServiceImpl::class.java)
    private var translationServiceImplForTest = TranslationServiceImpl(mappingService);

    @Test
    fun testTranslateRqaDefToResilienceTestConfig(){

        //arrange
        val systemId = "systemTestId"
        val domainId = "testDomainId"
        val systemForMapping = System()
        systemForMapping.id = systemId
        systemForMapping.type = "Process"
        systemForMapping.processName = "aTestingProcessName"
        val systemsSet = mutableSetOf(systemForMapping)
        val domainArchitectureMapping = DomainArchitectureMapping("testId", 1, "testContext", mutableSetOf(), mutableSetOf(), systemsSet)
        val artifact = Artifact(systemId, null)
        val stimulus = UnavailabilityStimulus(5,10)
        val responseMeasures = ResilienceResponseMeasures(Satisfaction.TOLERATED, null, null)
        val resilienceTestDefinition = ResilienceTestDefinition("TestDefinition", artifact, "TestDescription", stimulus, responseMeasures)
        val runtimeQualityAnalysis = RuntimeQualityAnalysis()
        runtimeQualityAnalysis.resilienceTests.add(resilienceTestDefinition)
        val rqaDefinition = RuntimeQualityAnalysisDefinition("testName", "1", Environment.DEV, domainId, "testContext", runtimeQualityAnalysis)

        whenever(mappingService.getMappingById(domainId)).thenReturn(domainArchitectureMapping)


        //act
        val result = translationServiceImplForTest.translateRqaDefToResilienceTestConfig(rqaDefinition)

        //assert
        assertAll(
                { Assertions.assertEquals("1", result.version)},
                { Assertions.assertEquals("testContext", result.context)},
                { Assertions.assertEquals("DEV", result.environment)},
                { Assertions.assertEquals("aTestingProcessName", result.enrichedResilienceTestDefinitions.first().enrichedProcessArtifact.processId)},
                { Assertions.assertEquals(systemId, result.enrichedResilienceTestDefinitions.first().enrichedProcessArtifact.systemId)},
                { Assertions.assertEquals(null, result.enrichedResilienceTestDefinitions.first().enrichedProcessArtifact.activityId)},
                { Assertions.assertEquals("TestDescription", result.enrichedResilienceTestDefinitions.first().description)},
                { Assertions.assertTrue(result.enrichedResilienceTestDefinitions.first().stimulus is UnavailabilityStimulus)},
                { Assertions.assertEquals(5, result.enrichedResilienceTestDefinitions.first().stimulus.pauseBeforeTriggeringSeconds)},
                { Assertions.assertEquals(10, result.enrichedResilienceTestDefinitions.first().stimulus.experimentDurationSeconds)},
                { Assertions.assertEquals(Satisfaction.TOLERATED, result.enrichedResilienceTestDefinitions.first().responseMeasure.recoveryTime)},
        )
    }

}
