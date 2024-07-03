package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition

fun interface RQATranslator {
  fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition, target: RQAConfiguration): RQAConfiguration
}
