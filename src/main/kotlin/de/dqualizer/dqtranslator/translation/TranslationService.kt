package de.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.loadtest.LoadTestConfig
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion

interface TranslationService {
    fun translate(rqaDefinition: RuntimeQualityAnalysisDefintion): LoadTestConfig
}
