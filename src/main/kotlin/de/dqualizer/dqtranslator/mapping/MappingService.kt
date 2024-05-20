package de.dqualizer.dqtranslator.mapping

import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping


interface MappingService {
    fun getMappingByContext(context: String): DomainArchitectureMapping

    fun getMappingById(id: String): DomainArchitectureMapping
}