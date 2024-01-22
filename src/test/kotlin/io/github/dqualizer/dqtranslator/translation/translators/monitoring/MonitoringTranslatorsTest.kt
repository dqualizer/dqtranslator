package io.github.dqualizer.dqtranslator.translation.translators.monitoring

import io.github.dqualizer.dqlang.types.dam.DomainArchitectureMapping
import io.github.dqualizer.dqlang.types.dam.architecture.*
import io.github.dqualizer.dqlang.types.dam.architecture.apischema.APISchema
import io.github.dqualizer.dqlang.types.dam.domainstory.*
import io.github.dqualizer.dqlang.types.dam.mapping.ActivityToCallMapping
import io.github.dqualizer.dqlang.types.dam.mapping.SystemToComponentMapping
import io.github.dqualizer.dqlang.types.rqa.configuration.monitoring.instrumentation.Instrument
import io.github.dqualizer.dqlang.types.rqa.definition.enums.Environment
import io.github.dqualizer.dqlang.types.rqa.definition.monitoring.MonitoringDefinition
import org.assertj.core.api.Assertions
import org.jeasy.random.EasyRandom
import org.junit.jupiter.api.Test
import java.lang.module.ModuleDescriptor
import java.net.URI


class MonitoringTranslatorsTest {

    val translator = MonitoringTranslators

    @Test
    fun canRegisterTranslator() {
        var random = EasyRandom()

        var dam = testData()

        var monitoringDefintions = random.objects(MonitoringDefinition::class.java, 3).toList()

        monitoringDefintions[0].target = dam.domainStory.activities[0].id
        monitoringDefintions[1].target = dam.domainStory.activities[1].id
        monitoringDefintions[2].target = dam.domainStory.actors[2].id


        val instruments: MutableMap<String, Set<Instrument>> = mutableMapOf()

        for (monitoringDefinition in monitoringDefintions) {
            val mappings = dam.mapper.getMappings(monitoringDefinition.target)

            val currentInstruments = translator.translate(monitoringDefinition, mappings.first(), dam)

            instruments += currentInstruments
        }

        Assertions.assertThat(true).isTrue()
    }

    fun testData(): DomainArchitectureMapping {

        val random = EasyRandom()

        //--- Domain Elements ---
        val actorsAndWorkObjects = listOf(
            System("company Service"),
            System("Assignment Service"),
            System("Master Data Service"),
            WorkObject("Master Data"),
            WorkObject("Assignment Data"),
            WorkObject("Company Created Event")
        ).map { it.apply { id = it.name } }

        val activities = listOf(
            Activity(
                "Activity3",
                "publishes",
                3,
                setOf(actorsAndWorkObjects[0].id),
                setOf(),
                setOf(actorsAndWorkObjects[5].id)
            ),
            Activity(
                "Activity4",
                "receives",
                4,
                setOf(actorsAndWorkObjects[1].id),
                setOf(),
                setOf(actorsAndWorkObjects[5].id)
            ),
            Activity(
                "Activity5",
                "requests",
                5,
                setOf(actorsAndWorkObjects[1].id),
                setOf(actorsAndWorkObjects[2].id),
                setOf(actorsAndWorkObjects[3].id)
            ),
            Activity(
                "Activity6",
                "searches",
                6,
                setOf(actorsAndWorkObjects[2].id),
                setOf(),
                setOf(actorsAndWorkObjects[3].id)
            ),
            Activity(
                "Activity7",
                "saves",
                7,
                setOf(actorsAndWorkObjects[1].id),
                setOf(),
                setOf(actorsAndWorkObjects[5].id)
            ),
        ).map { it.apply { id = it.name } }


        var dstElements = setOf(*(actorsAndWorkObjects + activities).toTypedArray())


        //-- Architecture Elements ---

        val codeComponents =
            listOf(
                CodeComponent(
                    "produceBlocking",
                    "KafkaGenerator#produceBlocking",
                    "KafkaGenerator.java",
                ),
                CodeComponent(
                    "processBbsMessage",
                    "CompanyChangesConsumer#processBbsMessage",
                    "CompanyChangesConsumer.java",
                ),
                CodeComponent(
                    "requestMasterData",
                    "MasterDataRequestProducer#requestMasterData",
                    "MasterDataRequestProducer#.java",
                ),
                CodeComponent(
                    "process",
                    "MasterDataResponseConsumer#process",
                    "MasterDataResponseConsumer.java",
                )
            ).map {
                it.apply { id = it.name }
            }

        val services =
            listOf(
                ServiceDescription(
                    "Company Service",
                    null,
                    URI.create("http://localhost:8080"),
                    ProgrammingFramework("Spring", ModuleDescriptor.Version.parse("2.5.5")),
                    "java",
                    InstrumentationFramework("ocelot"),
                    "platform1",
                    listOf(),
                    APISchema(),
                    listOf(codeComponents[0])
                ),
                ServiceDescription(
                    "Assignment Service",
                    null,
                    URI.create("http://localhost:8081"),
                    ProgrammingFramework("Spring", ModuleDescriptor.Version.parse("2.5.5")),
                    "java",
                    InstrumentationFramework("ocelot"),
                    "platform1",
                    listOf(),
                    APISchema(),
                    listOf(codeComponents[1], codeComponents[2], codeComponents[3])
                ),
                ServiceDescription(
                    "Master Data Service",
                    null,
                    URI.create("http://localhost:8082"),
                    ProgrammingFramework("Spring", ModuleDescriptor.Version.parse("2.5.5")),
                    "java",
                    InstrumentationFramework("ocelot"),
                    "platform1",
                    listOf(),
                    APISchema(),
                    listOf()
                ),
            ).map { it.apply { id = it.name } }


        // --- mappings ---


        val systemMappings = listOf(
            SystemToComponentMapping(actorsAndWorkObjects[0] as System, services[0]),
            SystemToComponentMapping(actorsAndWorkObjects[1] as System, services[1]),
            SystemToComponentMapping(actorsAndWorkObjects[2] as System, services[2]),
        )

        val activityToCallMappings = listOf(
            ActivityToCallMapping(activities[0], codeComponents[0]),
            ActivityToCallMapping(activities[1], codeComponents[1]),
            ActivityToCallMapping(
                activities[2],
                codeComponents[2],
                codeComponents[3],
                "message.getPayload().getCorrelationId()",
                "payload.getCorrelationId()"
            ),
        )

        val mappings = (systemMappings + activityToCallMappings).toSet()

        val softwareSystem = SoftwareSystem(
            "TestSystem",
            Environment.DEV,
            services,
            listOf(RuntimePlatform("platform1", "platform1", null))
        )

        val domainStory = DomainStory(
            actorsAndWorkObjects.filterIsInstance<Actor>(),
            actorsAndWorkObjects.filterIsInstance<WorkObject>(),
            activities,
        )

        val dam = DomainArchitectureMapping(
            softwareSystem,
            domainStory,
            mappings
        )

        return dam
    }
}
