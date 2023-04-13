package de.dqualizer.dqtranslator

data class EnvironmentNotFoundException(val environment: String): Exception("Environment $environment was not found")

data class ContextNotFoundException(val context: String): Exception("Context $context was not found")