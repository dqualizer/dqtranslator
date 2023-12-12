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
        systemForMapping.operationId = "aTestingProcessId"
        val systemsSet = mutableSetOf(systemForMapping)
        val domainArchitectureMapping = DomainArchitectureMapping("testId", 1, "testContext", mutableSetOf(), mutableSetOf(), systemsSet)
        val artifact = Artifact(systemId, null)
        val stimulus = UnavailabilityStimulus("Unavailability", 100)
        val responseMeasures = ResilienceResponseMeasures(Satisfaction.TOLERATED)
        val resilienceTestDefinition = ResilienceTestDefinition("TestDefinition", artifact, "TestDescription", stimulus, responseMeasures)
        val runtimeQualityAnalysis = RuntimeQualityAnalysis()
        runtimeQualityAnalysis.resilienceTests.add(resilienceTestDefinition)
        val rqaDefinition = RuntimeQualityAnalysisDefinition("testName", "1", Environment.DEV, domainId, "testContext", runtimeQualityAnalysis)

        whenever(mappingService.getMappingById(domainId)).thenReturn(domainArchitectureMapping)


        //act
        val result = translationServiceImplForTest.translateRqaDefToResilienceTestConfig(rqaDefinition)

        //assert
        // TODO assert single inner function calls, when method final
        assertAll(
                { Assertions.assertEquals("1", result.version)},
                { Assertions.assertEquals("testContext", result.context)},
                { Assertions.assertEquals("DEV", result.environment)},
                { Assertions.assertEquals("aTestingProcessId", result.enrichedResilienceTestDefinitions.first().artifact.processId)},
                { Assertions.assertEquals(systemId, result.enrichedResilienceTestDefinitions.first().artifact.systemId)},
                { Assertions.assertEquals(null, result.enrichedResilienceTestDefinitions.first().artifact.activityId)},
                { Assertions.assertEquals("TestDescription", result.enrichedResilienceTestDefinitions.first().description)},
                { Assertions.assertEquals("Unavailability", result.enrichedResilienceTestDefinitions.first().stimulus.type)},
                { Assertions.assertEquals(100, result.enrichedResilienceTestDefinitions.first().stimulus.accuracy)},
                { Assertions.assertEquals(Satisfaction.TOLERATED, result.enrichedResilienceTestDefinitions.first().responseMeasure.recoveryTime)},
        )
    }

}
