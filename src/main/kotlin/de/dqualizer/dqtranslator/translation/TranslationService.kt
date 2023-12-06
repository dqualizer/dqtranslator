package de.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.resilience.ResilienceTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition

interface TranslationService {
    fun translateRqaDefToLoadTestConfig(rqaDefinition: RuntimeQualityAnalysisDefinition): LoadTestConfiguration
    fun translateRqaDefToResilienceTestConfig(rqaDefinition: RuntimeQualityAnalysisDefinition): ResilienceTestConfiguration
}