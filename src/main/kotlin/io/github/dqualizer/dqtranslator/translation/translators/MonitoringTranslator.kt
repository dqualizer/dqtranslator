package io.github.dqualizer.dqtranslator.translation.translators

import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.architecture.CodeComponent
import io.github.dqualizer.dqlang.types.dam.architecture.ServiceDescription
import io.github.dqualizer.dqlang.types.dam.domainstory.DSTElement
import io.github.dqualizer.dqlang.types.dam.mapping.*
import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.MonitoringConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.ServiceMonitoringConfiguration
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.instrumentation.Instrument
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.instrumentation.InstrumentLocation
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.instrumentation.InstrumentType
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.monitoring.MeasurementType
import io.github.dqualizer.dqlang.types.rqa.definition.monitoring.MonitoringDefinition
import io.github.dqualizer.dqtranslator.mapping.MappingServiceImpl
import io.github.dqualizer.dqtranslator.translation.RQATranslator
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

        val instruments = mutableMapOf<ServiceDescription, Instrument>()

        for (monitoring in rqaDefinition.runtimeQualityAnalysis.monitoringDefinition) {

            val targetDstEntity: DSTElement = dam.domainStory.findElementById(monitoring.target)
            val targetArchitectureEntity = mapper.mapToArchitecturalEntity(targetDstEntity)

            val mapping = mapper.getMappings(targetDstEntity).firstOrNull()
                ?: throw NoSuchElementException("No mapping for DST element ${targetDstEntity.id} found.")


            when (mapping) {

                is SystemToComponentMapping -> {

                }

                is WorkObjectToTypeMapping -> {

                }

                is ActivityToCallMapping -> {

                }

                else -> {
                    log.error("Cannot translate mapping of type ${mapping.javaClass}")
                    continue
                }


            }
        }

        for ((service, serviceInstruments) in instruments) {
            log.debug("Adding instruments {} to service {}", serviceInstruments, service)
//            serviceInstrumentes.computeIfAbsent(service) { mutableSetOf() }.addAll(serviceInstruments)
        }

        val serviceMonitoringConfigurations = serviceInstrumentes.mapValues { (serviceName, instruments) ->
            ServiceMonitoringConfiguration(
                serviceName,
                instruments.toSet(),
                serviceMonitoringFrameworks[serviceName]!!
            )
        }

        target.monitoringConfiguration = MonitoringConfiguration(serviceMonitoringConfigurations.values)
        return target
    }



}
