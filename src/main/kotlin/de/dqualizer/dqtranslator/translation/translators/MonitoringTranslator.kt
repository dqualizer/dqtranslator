package de.dqualizer.dqtranslator.translation.translators

import de.dqualizer.dqtranslator.translation.RQATranslator
import io.github.dqualizer.dqlang.archive.loadtesttranslator.dqlang.modeling.RuntimeQualityAnalysisDefintion
import io.github.dqualizer.dqlang.types.rqa.RQAConfiguration
import org.springframework.stereotype.Service

/**
 * @author Lion Wagner
 */
@Service
class MonitoringTranslator : RQATranslator {
    override fun translate(rqaDefinition: RuntimeQualityAnalysisDefintion, target: RQAConfiguration): RQAConfiguration {
        TODO("Not yet implemented")
    }
}
