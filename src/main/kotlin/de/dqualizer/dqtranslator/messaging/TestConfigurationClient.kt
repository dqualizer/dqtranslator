package de.dqualizer.dqtranslator.messaging

import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration


interface TestConfigurationClient {
    fun queueLoadTestConfiguration(loadTestConfiguration: LoadTestConfiguration)
}