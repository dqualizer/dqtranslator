package de.dqualizer.dqtranslator.translation

import dqualizer.dqlang.archive.loadtesttranslator.dqlang.loadtest.LoadTestConfig
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion

interface TranslationService {
    fun translate(rqaDefinition: RuntimeQualityAnalysisDefintion): LoadTestConfig
}