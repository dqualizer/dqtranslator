package io.github.dqualizer.dqtranslator.translation.translators

import io.github.dqualizer.dqlang.draft.rqaDefinition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.instrumentation.*
import io.github.dqualizer.dqlang.types.rqa.configuration.RQAConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.MeasurementType
import io.github.dqualizer.dqtranslator.ServiceNotFoundException
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


    override fun translate(rqaDefinition: RuntimeQualityAnalysisDefinition, target: RQAConfiguration): RQAConfiguration {
        if(rqaDefinition.runtimeQualityAnalysis.monitoring.size <= 0){
            log.debug("No monitoring definitions found in RQA Definition")
            return target
        }


        val dam = mappingService.getDAMByContext(rqaDefinition.domainId)
        val services = dam.softwareSystem.services.associateBy({ it.name }, { it })


        val serviceInstrumentes = mutableMapOf<String, MutableList<Instrument>>()
        val serviceMonitoringFrameworks = dam.softwareSystem.services.associateBy({ it.name }, { it.instrumentationFramework })

        for (monitoring in rqaDefinition.runtimeQualityAnalysis.monitoring) {

            val technicalEntity = dam.systems.find { it.id == monitoring.target }!!
            val naming = mappingService.resolveName(technicalEntity.operationId)
            val serviceName = naming.serviceId.orElseThrow { ServiceNotFoundException(technicalEntity.operationId) }

            val location = InstrumentLocation(naming.functionHolderName, technicalEntity.operationId)

            val instrumentType = when (monitoring.measurementType) {
                MeasurementType.EXECUTION_TIME -> InstrumentType.GAUGE
                MeasurementType.EXECUTION_COUNT -> InstrumentType.COUNTER
                MeasurementType.VALUE_INSPECTION -> InstrumentType.GAUGE
            }

            val sanitizedName = sanitizeName(monitoring.measurementName)

            val instrumentName = sanitizedName + "_i1"

            if (!VALID_INSTRUMENT_NAME_PATTERN.matcher(instrumentName).matches())
                throw IllegalArgumentException("Instrument name $instrumentName does not match the opentelemetry spec.")

            val instrument = Instrument(
                setOf(),
                sanitizedName,
                instrumentName,
                instrumentType,
                monitoring.measurementType,
                location
            )

            val instruments = serviceInstrumentes.computeIfAbsent(serviceName) { mutableListOf() }
            instruments.add(instrument)
        }

        val serviceMonitoringConfigurations = serviceInstrumentes.mapValues { (serviceName, instruments) ->
            ServiceMonitoringConfiguration(serviceName, instruments, serviceMonitoringFrameworks[serviceName]!!)
        }

        target.monitoringConfiguration = MonitoringConfiguration(
            serviceMonitoringConfigurations.values,
            serviceMonitoringFrameworks.filter { serviceInstrumentes.keys.contains(it.key) })
        return target
    }

    fun sanitizeName(str: String): String {
        return str.trim().lowercase().replace(Regex("\\s"), "_")
    }


}
