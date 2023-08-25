package io.github.dqualizer.dqtranslator.mapping

import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping


interface MappingService {
    fun getDAMByContext(context: String): DomainArchitectureMapping
}
