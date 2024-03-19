package io.github.dqualizer.dqtranslator.translation.translators

import io.github.dqualizer.dqlang.types.dam.domainstory.DSTElement
import io.github.dqualizer.dqlang.types.dam.mapping.*
import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.MonitoringConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.ServiceMonitoringConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.instrumentation.Instrument
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqtranslator.mapping.MappingServiceImpl
import io.github.dqualizer.dqtranslator.translation.RQATranslator
import io.github.dqualizer.dqtranslator.translation.translators.monitoring.MonitoringTranslators
import org.springframework.stereotype.Service
import java.util.regex.Pattern

/**
 * @author Lion Wagner
 */
@Service
class MonitoringTranslator(
    val mappingService: MappingServiceImpl
) : RQATranslator {

    val log = org.slf4j.LoggerFactory.getLogger(javaClass)

    companion object {
        /**
         * Opentelemetry Spec definition of valid instrument name
         */
        val VALID_INSTRUMENT_NAME_PATTERN: Pattern =
            Pattern.compile("([A-Za-z])([A-Za-z0-9_\\-.]){0,254}")
    }


    override fun translate(
        rqaDefinition: RuntimeQualityAnalysisDefinition,
        target: RQAConfiguration
    ): RQAConfiguration {
        if (rqaDefinition.runtimeQualityAnalysis.monitoringDefinition.size <= 0) {
            log.debug("No monitoring definitions found in RQA Definition")
            return target
        }

        val dam = mappingService.getDAMByContext(rqaDefinition.domainId)
        val mapper = DAMapper(dam, false)

        val serviceInstrumentes = mutableMapOf<String, MutableSet<Instrument>>()
        val serviceMonitoringFrameworks =
            dam.softwareSystem.services.associateBy({ it.name }, { it.instrumentationFramework })


        for (monitoring in rqaDefinition.runtimeQualityAnalysis.monitoringDefinition) {

            val targetDstEntity: DSTElement = dam.domainStory.findElementByName(monitoring.target)
            val targetArchitectureEntity = mapper.mapToArchitecturalEntity(targetDstEntity)

            val mapping = mapper.getMappings(targetDstEntity).firstOrNull()
                ?: throw NoSuchElementException("No mapping for DST element ${targetDstEntity.id} found.")


            val instrumentsPerService = MonitoringTranslators.translate(monitoring, mapping, dam)

            println(instrumentsPerService)

            for ((service, instruments) in instrumentsPerService) {
                serviceInstrumentes.computeIfAbsent(service) { mutableSetOf() }.addAll(instruments)
            }
        }


        val serviceMonitoringConfigurations = serviceInstrumentes.mapValues { (serviceName, instruments) ->
            ServiceMonitoringConfiguration(
                dam.softwareSystem.findServiceByName(serviceName).get().id,
                instruments.toSet(),
                serviceMonitoringFrameworks[serviceName]!!
            )
        }

        target.monitoringConfiguration = MonitoringConfiguration(serviceMonitoringConfigurations.values)
        return target
    }
}
