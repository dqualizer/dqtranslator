package io.github.dqualizer.dqtranslator.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqtranslator.ContextNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Service
import java.util.*

@Service
@Deprecated("Use DAMMongoRepository instead to retrieve DAM")
class MappingServiceImpl(
  private val objectMapper: ObjectMapper,
  @Qualifier("webApplicationContext") private val resourceLoader: ResourceLoader
) : MappingService {
  private val log = LoggerFactory.getLogger(MappingServiceImpl::class.java)

  @Value("\${dqualizer.mappingDirectory}")
  private lateinit var mappingDirectory: String

  override fun getDAMByContext(context: String): DomainArchitectureMapping {
    log.info("Trying to load context=$context from directory=classpath:$mappingDirectory")

    return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
      .getResources("classpath:$mappingDirectory/*.json")
      .map { it.inputStream.bufferedReader().readText() }
      .map { objectMapper.readValue(it, DomainArchitectureMapping::class.java) }
      .firstOrNull { it.id == context } ?: throw ContextNotFoundException(context)
  }

  /**
   * Parses a qualified name string into a ResolvedQualifiedName object.
   *
   * Expected Syntax is <SERVICE>:<FUNCTIONHOLDER>#<FUNCTIONNAME>(<Parameter1>,<Parameter2>,...)
   *
   * All properties except the FUNCTIONHOLDER may be omitted.
   */
  fun resolveName(qualifiedName: String): ResolvedQualifiedName {
    val service = if (qualifiedName.contains(":")) qualifiedName.substringBefore(":") else null

    val functionHolder = qualifiedName.substringAfter(":").substringBefore("#")
    val functionName = if (qualifiedName.contains("#"))
      qualifiedName.substringAfter("#").substringBefore("(") else null
    val parameters = if (qualifiedName.contains("("))
      qualifiedName.substringAfter("(").substringBefore(")").split(",") else null

    return ResolvedQualifiedName(
      Optional.ofNullable(service),
      functionHolder,
      Optional.ofNullable(functionName),
      Optional.ofNullable(parameters)
    )
  }
}
