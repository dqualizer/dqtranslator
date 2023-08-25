package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.draft.rqaDefinition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration

fun interface RQATranslator {
    fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition, target: RQAConfiguration): RQAConfiguration
}
