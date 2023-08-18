package io.github.dqualizer.dqtranslator.mapping

import io.github.dqualizer.dqlang.types.domain_architecture_mapping.DomainArchitectureMapping


interface MappingService {
    fun getMappingByContext(context: String): DomainArchitectureMapping
}
