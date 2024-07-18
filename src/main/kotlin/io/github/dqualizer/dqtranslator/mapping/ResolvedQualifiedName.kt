package io.github.dqualizer.dqtranslator.mapping

import java.util.*

data class ResolvedQualifiedName(
  val serviceId: Optional<String>,
  /**
   * Name of the entity that can "own" a function.
   * This may be a class in OO languages, a module, a file or something similar.
   */
  val functionHolderName: String,
  val functionName: Optional<String>,
  val functionSignature: Optional<List<String>>
)


