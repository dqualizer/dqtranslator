package de.dqualizer.dqtranslator.mapping

import dqualizer.dqlang.archive.loadtesttranslator.dqlang.domainarchitecturemapping.DomainArchitectureMapping

interface MappingService {
    fun getMappingByContext(context: String): DomainArchitectureMapping
}