package io.github.dqualizer.dqtranslator.translation

import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition

class RQATranslationChain {
  val log = org.slf4j.LoggerFactory.getLogger(javaClass)

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
    log.info("RuntimeQualityAnalysisDefinition: $rqaDefinition")
    var rqaConfiguration = RQAConfiguration(context = rqaDefinition.context)

    for (translator in translators) {
      rqaConfiguration = translator.translate(rqaDefinition, rqaConfiguration)
    }
    return rqaConfiguration
  }
}
