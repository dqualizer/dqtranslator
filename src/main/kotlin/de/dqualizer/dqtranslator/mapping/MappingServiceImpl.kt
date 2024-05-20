package de.dqualizer.dqtranslator.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import de.dqualizer.dqtranslator.ContextNotFoundException
import io.github.dqualizer.dqlang.types.dam.Activity
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.Endpoint
import io.github.dqualizer.dqlang.types.dam.ServerInfo
import io.github.dqualizer.dqlang.types.dam.System

import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
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

    @Value("\${dqualizer.dqapi.host}")
    private lateinit var dqApiHost : String;
    @Value("\${dqualizer.dqapi.port}")
    private lateinit var dqApiPort : String;

    val client = HttpClient {
        install(Logging)
    };

    override fun getMappingByContext(context: String): DomainArchitectureMapping {
        log.info("Trying to load context=$context from directory=classpath:$mappingDirectory")

        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResources("classpath:$mappingDirectory/*.json")
            .map { it.inputStream.bufferedReader().readText() }
            .map { objectMapper.readValue(it, DomainArchitectureMapping::class.java) }
            .firstOrNull { it.context == context } ?: throw ContextNotFoundException(context)
    }

    override fun getMappingById(id: String): DomainArchitectureMapping {
        // Define a variable to hold the result
        var result: DomainArchitectureMapping? = null

        runBlocking {
            val damResponse = client.get("http://${dqApiHost}:${dqApiPort}/api/v1/dam/$id")
            result = objectMapper.readValue(damResponse.bodyAsText(), DomainArchitectureMapping::class.java)
            log.info(damResponse.bodyAsText());
        }

        // Return the result or throw an error if it's null
        return result ?: throw IllegalStateException("Failed to fetch DomainArchitectureMapping for ID: $id")
    }

    override fun getMappingByIdHardcoded(id: String): DomainArchitectureMapping {

        val serverInfo = ServerInfo()
        serverInfo.id = "testServerId"
        serverInfo.environment = "DEV"
        serverInfo.host = "http://localhost:3000/"

        val systemForMapping = System()
        systemForMapping.id = "MyComputer"
        systemForMapping.type = "Process"
        systemForMapping.processName = "KeePassXC.exe"
        systemForMapping.processPath = """C:\Program Files\KeePassXC.exe"""
        val activity = Activity()
        val endpoint = Endpoint()
        endpoint.field = "/"
        endpoint.operation = "GET"
        endpoint.path_variables = listOf()
        endpoint.url_parameter = listOf()
        endpoint.request_parameter = listOf()
        endpoint.payloads = listOf()
        endpoint.responses = listOf()
        activity.endpoint = endpoint
        systemForMapping.activities = listOf(activity)

        return DomainArchitectureMapping("testId", 1, "testContext", mutableSetOf(serverInfo), mutableSetOf(), setOf(systemForMapping))
    }

}
