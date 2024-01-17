package io.github.dqualizer.dqtranslator.translation.translators.monitoring

import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.architecture.CodeComponent
import io.github.dqualizer.dqlang.types.dam.mapping.ActivityToCallMapping
import io.github.dqualizer.dqlang.types.dam.mapping.DAMapping
import io.github.dqualizer.dqlang.types.dam.mapping.SystemToComponentMapping
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.instrumentation.Instrument
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.instrumentation.InstrumentLocation
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.instrumentation.InstrumentType
import io.github.dqualizer.dqlang.types.rqa.definition.monitoring.MeasurementType
import io.github.dqualizer.dqlang.types.rqa.definition.monitoring.MonitoringDefinition
import java.util.stream.Collectors


fun sanitizeName(str: String): String {
    return str.trim().lowercase().replace(Regex("\\s"), "_")
}

object MonitoringTranslators {
    private val translators = listOf(
        ActivityToCallTranslator,
        ServiceToServiceTranslator
    )

    private val translatorMap = translators.stream()
        .collect(Collectors.toMap({ it.targetClass() }, { it }))
            as Map<Class<in DAMapping>, MonitoringTranslator<DAMapping>>

    fun translate(
        monitoring: MonitoringDefinition,
        mapping: DAMapping,
        dam: DomainArchitectureMapping
    ): Map<String, Set<Instrument>> {
        val translator = translatorMap[mapping.javaClass]
            ?: throw IllegalArgumentException("No translator found for mapping type ${mapping.javaClass}")

        return translator.translate(monitoring, mapping, dam)
    }
}

private interface MonitoringTranslator<T : DAMapping> {

    /**
     * @return a map of which instruments to place in which service (by id)
     */
    fun translate(
        monitoring: MonitoringDefinition,
        mapping: T,
        dam: DomainArchitectureMapping
    ): Map<String, Set<Instrument>>

    fun targetClass(): Class<T>

}

private object ActivityToCallTranslator : MonitoringTranslator<ActivityToCallMapping> {
    override fun translate(
        monitoring: MonitoringDefinition,
        mapping: ActivityToCallMapping,
        dam: DomainArchitectureMapping
    ): Map<String, Set<Instrument>> {

        val mappingTarget = mapping.getArchitectureEntity(dam.softwareSystem)
        when (mappingTarget) {
            is CodeComponent -> {
                return createInstrumentFromCodeComponent(mappingTarget, dam, monitoring)
            }

            else -> {
                throw IllegalArgumentException("Mapping target is not a CodeComponent")
            }
        }
    }

    override fun targetClass(): Class<ActivityToCallMapping> {
        return ActivityToCallMapping::class.java
    }

    fun createInstrumentFromCodeComponent(
        codeComponent: CodeComponent,
        domainArchitectureMapping: DomainArchitectureMapping,
        monitoringDefinition: MonitoringDefinition
    ): Map<String, Set<Instrument>> {

        val service =
            domainArchitectureMapping.softwareSystem.services.find { it.codeComponents.contains(codeComponent) }!!

        val location = InstrumentLocation(codeComponent.file, codeComponent.identifier)

        val instrumentType = when (monitoringDefinition.measurementType) {
            MeasurementType.EXECUTION_TIME -> InstrumentType.GAUGE
            MeasurementType.EXECUTION_COUNT -> InstrumentType.COUNTER
            MeasurementType.VALUE_INSPECTION -> InstrumentType.GAUGE
        }

        val sanitizedName = sanitizeName(monitoringDefinition.measurementName)

        val instrumentName = sanitizedName + "_i1"

        if (!io.github.dqualizer.dqtranslator.translation.translators.MonitoringTranslator.VALID_INSTRUMENT_NAME_PATTERN.matcher(
                instrumentName
            ).matches()
        ) {
            throw IllegalArgumentException("Instrument name $instrumentName does not match the opentelemetry spec.")
        }

        val instrument = Instrument(
            mapOf(
                "monitoring_id" to monitoringDefinition.id
            ),
            sanitizedName,
            instrumentName,
            instrumentType,
            monitoringDefinition.measurementType,
            monitoringDefinition.measurementUnit,
            location
        )

        return mapOf(service.name to setOf(instrument))
    }
}

private object ServiceToServiceTranslator : MonitoringTranslator<SystemToComponentMapping> {
    override fun translate(
        monitoring: MonitoringDefinition,
        mapping: SystemToComponentMapping,
        dam: DomainArchitectureMapping
    ): Map<String, Set<Instrument>> {
        return mapOf()
    }

    override fun targetClass(): Class<SystemToComponentMapping> {
        return SystemToComponentMapping::class.java
    }
}


