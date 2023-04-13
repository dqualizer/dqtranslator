package de.dqualizer.dqtranslator.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import de.dqualizer.dqtranslator.ContextNotFoundException
import dqualizer.dqlang.archive.loadtesttranslator.dqlang.domainarchitecturemapping.DomainArchitectureMapping
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

@Service
class MappingServiceImpl (
        private val objectMapper: ObjectMapper
)  : MappingService{
    @Value("\${dqualizer.mappingDirectory}")
    private lateinit var mappingDirectory: String

    private val mappingPath: String
        get() = this::class.java.classLoader.getResource("").file.substring(1) + mappingDirectory

    override fun getMappingByContext(context: String): DomainArchitectureMapping {
        return File(mappingPath).walk()
                .filter { it.isFile }
                .map { Files.readString(it.toPath()) }
                .map { objectMapper.readValue(it, DomainArchitectureMapping::class.java) }
                .firstOrNull { it.context == context } ?: throw ContextNotFoundException(context)
    }
}