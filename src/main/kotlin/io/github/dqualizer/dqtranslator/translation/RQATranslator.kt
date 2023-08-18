package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion
import io.github.dqualizer.dqlang.types.rqa.RQAConfiguration

fun interface RQATranslator {
    fun translate(rqaDefinition: RuntimeQualityAnalysisDefintion, target: RQAConfiguration): RQAConfiguration
}
