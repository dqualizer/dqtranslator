package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.draft.rqaDefinition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration


interface TranslationService {
    fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition): LoadTestConfiguration
}
