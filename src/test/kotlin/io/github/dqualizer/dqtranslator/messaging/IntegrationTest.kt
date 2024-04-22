package io.github.dqualizer.dqtranslator.messaging

import io.github.dqualizer.dqlang.data.DAMRepository
import io.github.dqualizer.dqlang.messaging.AMQPAutoConfiguration
import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.domainstory.Actor
import io.github.dqualizer.dqlang.types.dam.domainstory.Group
import io.github.dqualizer.dqlang.types.dam.domainstory.Person
import io.github.dqualizer.dqlang.types.dam.domainstory.System
import io.github.dqualizer.dqlang.types.rqa.configuration.loadtest.LoadTestConfiguration
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.github.dqualizer.dqlang.types.rqa.definition.stimulus.loadprofile.ConstantLoad
import io.github.dqualizer.dqlang.types.rqa.definition.stimulus.loadprofile.LoadIncrease
import io.github.dqualizer.dqlang.types.rqa.definition.stimulus.loadprofile.LoadPeak
import io.github.dqualizer.dqlang.types.rqa.definition.stimulus.loadprofile.LoadProfile
import io.github.dqualizer.dqtranslator.translation.RQATranslationChain
import io.github.dqualizer.dqtranslator.translation.RQATranslator
import org.assertj.core.api.Assertions.assertThat
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.randomizers.AbstractRandomizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.messaging.MessageHeaders
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.RabbitMQContainer
import java.io.File
import java.lang.module.ModuleDescriptor.Version
import java.util.*

class VersionRandomizer : AbstractRandomizer<Version>() {
    override fun getRandomValue(): Version {
        return Version.parse("${random.nextInt(10)}.${random.nextInt(10)}.${random.nextInt(10)}")
    }
}


@SpringBootTest
class IntegrationTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {

            val rabbit = RabbitMQContainer("rabbitmq:management-alpine")
            rabbit.portBindings = listOf("5672:5672", "15672:15672")
            rabbit.start()

            val mongoDBContainer = MongoDBContainer("mongo:7")
            mongoDBContainer.portBindings = listOf("27017:27017")
            mongoDBContainer.start()
        }
    }

    @Autowired
    lateinit var translators: List<RQATranslator>

    @Autowired
    lateinit var rqaConfigurationProducer: RQAConfigurationProducer

    @Autowired
    lateinit var damStore: DAMRepository

    var generator = EasyRandom(
        EasyRandomParameters().randomize(LoadProfile::class.java) {
            return@randomize EasyRandom().nextObject(
                listOf(
                    LoadPeak::class.java,
                    LoadIncrease::class.java,
                    ConstantLoad::class.java
                ).random()
            )
        }.randomize(Actor::class.java) {
            return@randomize EasyRandom().nextObject(
                listOf(
                    Person::class.java,
                    Group::class.java,
                    System::class.java
                ).random()
            )
        }.randomize(HttpMethod::class.java) {
            return@randomize HttpMethod.values().random()
        }.randomize({
            it.name == "number"
        }, {
            return@randomize Random().nextInt(100)
        }).randomize(Version::class.java, VersionRandomizer())
    )


    @BeforeEach
    fun beforeEach() {
        damStore.clear()
    }


    data class TestData(val rqaD: RuntimeQualityAnalysisDefinition, val dam: DomainArchitectureMapping)

    private fun loadTestData(): TestData {
        val objectMapper = AMQPAutoConfiguration().objectMapper()
        val rqa: RuntimeQualityAnalysisDefinition = objectMapper.readValue(
            File("src/test/resources/mock/RQA.json"),
            RuntimeQualityAnalysisDefinition::class.java
        )
        val dam: DomainArchitectureMapping = objectMapper.readValue(
            File("src/test/resources/mock/MockMapping.json"),
            DomainArchitectureMapping::class.java
        )

        assertThat(rqa).isNotNull
        assertThat(dam).isNotNull

        return TestData(rqa, dam)
    }


    @Test
    fun canLoadTestData() {
        val data = loadTestData()
        assertThat(data).isNotNull
    }

    @Test
    fun canTranslateTestData() {
        val data = loadTestData()

        damStore.storeDAM(data.dam)

        val rqaWithCorrectContext = RuntimeQualityAnalysisDefinition(
            data.rqaD.name,
            data.rqaD.version,
            data.rqaD.domainId,
            data.rqaD.context,
            data.rqaD.environment,
            data.rqaD.runtimeQualityAnalysis
        )


        val rqaConfiguration = RQATranslationChain()
            .chain(translators)
            .translate(rqaWithCorrectContext)

        assertThat(rqaConfiguration).isNotNull
    }


    @Test
    fun canSendTranslationToQueue() {
        val data = loadTestData()
        damStore.storeDAM(data.dam)


        val rqaConfiguration = RQATranslationChain()
            .chain(translators)
            .translate(data.rqaD)

        rqaConfiguration.loadConfiguration = generator.nextObject(LoadTestConfiguration::class.java)
        rqaConfiguration.context = data.dam.id!!

        for (i in 0..<1) {
            rqaConfigurationProducer.produce(rqaConfiguration, MessageHeaders(mapOf()))
        }

        assertThat(rqaConfiguration).isNotNull
    }
}

