package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition

class RQATranslationChain {

    private val translators: MutableList<RQATranslator> = mutableListOf()

    fun chain(translator: RQATranslator): RQATranslationChain {
        translators.add(translator)
        return this
    }

    fun chain(translators: Collection<RQATranslator>): RQATranslationChain {
        translators.forEach { chain(it) }
        return this
    }

    fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition): RQAConfiguration {
        var loadTestConfig = RQAConfiguration(context = rqaDefinition.context)
        for (translator in translators) {
            loadTestConfig = translator.translate(rqaDefinition, loadTestConfig)
        }
        return loadTestConfig
    }
}
