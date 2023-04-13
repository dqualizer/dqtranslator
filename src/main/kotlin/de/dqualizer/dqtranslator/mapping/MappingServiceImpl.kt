package de.dqualizer.dqtranslator.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import de.dqualizer.dqtranslator.ContextNotFoundException
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.domainarchitecturemapping.DomainArchitectureMapping
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

@Service
class MappingServiceImpl (
        private val objectMapper: ObjectMapper
)  : MappingService{
    private val log = LoggerFactory.getLogger(MappingServiceImpl::class.java)

    @Value("\${dqualizer.mappingDirectory}")
    private lateinit var mappingDirectory: String

    private val mappingPath: String
        get() = this::class.java.classLoader.getResource("").file.substring(1) + mappingDirectory

    override fun getMappingByContext(context: String): DomainArchitectureMapping {
        val json = MappingServiceImpl::class.java.getResource("/$mappingDirectory/mapping-werkstatt.json")
        return objectMapper.readValue(json, DomainArchitectureMapping::class.java)
    }
}