package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import org.springframework.stereotype.Service

@Service
class TranslationService(
    private val rqaTranslators: List<RQATranslator>,
) {
    fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition): RQAConfiguration {
        //translate rqa definition to rqa configuration
        val rqaConfiguration = RQATranslationChain()
            .chain(rqaTranslators)
            .translate(rqaDefinition)

        return rqaConfiguration
    }

}
