package de.dqualizer.dqtranslator.backmapping

import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import reactor.core.publisher.Mono

@Component
class GrafanaApiClient(private val webClientBuilder: WebClient.Builder,
                       @Value("\${grafana.api.url}") private val grafanaApiUrl: String,
                       @Value("\${grafana.api.token}") private val grafanaApiToken: String) {

    private val webClient: WebClient = webClientBuilder
        .baseUrl(grafanaApiUrl)
        .defaultHeader("Authorization", "Bearer $grafanaApiToken")
        .build()

    fun createDashboard(dashboardJson: String): Mono<String> {
        return webClient.post()
            .uri("/dashboards/db")
            .bodyValue(dashboardJson)
            .retrieve()
            .bodyToMono(String::class.java)
    }

    suspend fun getOrganizations(): Flow<Organization> {
        return webClient.get()
            .uri("/orgs")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlow<Organization>()
    }
}