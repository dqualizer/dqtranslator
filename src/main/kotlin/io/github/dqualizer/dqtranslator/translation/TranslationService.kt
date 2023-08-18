package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion
import io.github.dqualizer.dqlang.types.load_test_configuration.LoadTestConfiguration

interface TranslationService {
    fun translate(rqaDefinition: RuntimeQualityAnalysisDefintion): LoadTestConfiguration
}
