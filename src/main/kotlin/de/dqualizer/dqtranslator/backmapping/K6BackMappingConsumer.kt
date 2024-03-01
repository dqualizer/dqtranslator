package de.dqualizer.dqtranslator.backmapping

import com.fasterxml.jackson.databind.ObjectMapper
import com.influxdb.client.InfluxDBClient
import de.dqualizer.dqtranslator.mapping.MappingService
import io.github.dqualizer.dqlang.types.rqa.definition.RuntimeQualityAnalysisDefinition
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.util.logging.Logger


@Service
class K6BackMappingConsumer(
    private val mappingService: MappingService,
    private val objectMapper: ObjectMapper,
    @field:Autowired private val influxDBClient: InfluxDBClient,
) {
    private val logger = Logger.getLogger(this.javaClass.name)

    @Autowired
    private lateinit var grafanaApiClient: GrafanaApiClient

    @Value("\${grafana.api.token}")
    private lateinit var grafana_token: String

    @Value("\${dqualizer.dqapi.host}")
    private lateinit var dqApiHost: String;

    @Value("\${dqualizer.dqapi.port}")
    private lateinit var dqApiPort: String;

    val client = HttpClient {
        install(Logging)
    };

    /**
     * Import the k6 configuration and start the configuration runner
     *
     * @param config An inofficial k6 configuration
     */
    @RabbitListener(queues = ["\${dqualizer.messaging.queues.k6.back-mapping}"])
    fun receive(
        @Payload rqaId: String,
    ) {
        logger.info("Received rqa id  $rqaId")

        println(influxDBClient.ping())

        runBlocking {
            val rqaResponse = client.get("http://${dqApiHost}:${dqApiPort}/api/v1/rqa-definition/$rqaId")
            val rqaDef = objectMapper.readValue(rqaResponse.bodyAsText(), RuntimeQualityAnalysisDefinition::class.java)
            val dam = mappingService.getMappingById(rqaDef.domainId)

            rqaDef.runtimeQualityAnalysis.loadtests.forEach {
                val flux = """
                    from(bucket:"my-bucket")
                        |> range(start: 0)
                """.trimIndent()

                val queryApi = influxDBClient.queryApi

                val tables = queryApi.query(flux)
//                for (fluxTable in tables) {
//                    val records = fluxTable.records
//                    for (fluxRecord in records) {
//                        println(fluxRecord.time.toString() + ": " + fluxRecord.getValueByKey("_value"))
//                    }
//                }


                println(grafana_token)
                val organizations = grafanaApiClient.getOrganizations()

                println(organizations)

//                val testQuery = "from(bucket:\"my-bucket\") |> range(start: 0)"
//                val rawQuery = """
//                from(bucket: "$bucket")
//                  |> range(start: 0)
//                  |> filter(fn: (r) => r["systemId"] == "${it.artifact.systemId}")
//                  |> filter(fn: (r) => r["domainId"] == "${rqaDef.domainId}")
//                  |> filter(fn: (r) => r["rqaId"] == "$rqaId")
//                  |> filter(fn: (r) => r["activityId"] == "${it.artifact.activityId}")
//                  |> filter(fn: (r) => r["_measurement"] == "http_req_duration")
//            """.trimIndent()
//                logger.info(rawQuery)
//                it.responseMeasures.responseTime
//                val query = queryApi.query(testQuery)
//                logger.info("Got result $query")
            }


        }


    }
}

