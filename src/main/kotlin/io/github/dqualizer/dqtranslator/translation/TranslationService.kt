package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration


interface TranslationService {
    fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition): LoadTestConfiguration
}
