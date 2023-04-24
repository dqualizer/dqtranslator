package de.dqualizer.dqtranslator.messaging

import dqualizer.dqlang.archive.loadtesttranslator.dqlang.loadtest.LoadTestConfig

interface TestConfigurationClient {
    fun queueLoadTestConfiguration(loadTestConfiguration: LoadTestConfig)
}