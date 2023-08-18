package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion
import io.github.dqualizer.dqlang.types.rqa.RQAConfiguration


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

    fun translate(rqaDefinition: RuntimeQualityAnalysisDefintion): RQAConfiguration {
        var loadTestConfig = RQAConfiguration()
        for (translator in translators) {
            loadTestConfig = translator.translate(rqaDefinition, loadTestConfig)
        }
        return loadTestConfig
    }
}
