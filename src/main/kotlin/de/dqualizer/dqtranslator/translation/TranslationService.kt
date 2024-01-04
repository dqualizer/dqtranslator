package de.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition

interface TranslationService {
    fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition): LoadTestConfiguration
}