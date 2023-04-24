package de.dqualizer.dqtranslator.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import de.dqualizer.dqtranslator.ContextNotFoundException
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.domainarchitecturemapping.DomainArchitectureMapping
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Service

@Service
class MappingServiceImpl(
    private val objectMapper: ObjectMapper,
    private val resourceLoader: ResourceLoader
) : MappingService {
    private val log = LoggerFactory.getLogger(MappingServiceImpl::class.java)

    @Value("\${dqualizer.mappingDirectory}")
    private lateinit var mappingDirectory: String

    override fun getMappingByContext(context: String): DomainArchitectureMapping {
        log.info("Trying to load context=$context from directory=classpath:$mappingDirectory")

        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResources("classpath:$mappingDirectory/*.json")
            .map { it.inputStream.bufferedReader().readText() }
            .map { objectMapper.readValue(it, DomainArchitectureMapping::class.java) }
            .firstOrNull { it.context == context } ?: throw ContextNotFoundException(context)
    }
}
