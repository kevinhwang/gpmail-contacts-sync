package kevinhwang.gpmailcontactssync.tribe

import kevinhwang.gpmailcontactssync.TribeConfigurationProperties
import kevinhwang.gpmailcontactssync.tribe.model.list.ListedPerson
import kevinhwang.gpmailcontactssync.tribe.model.person.Person
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.constraints.NotEmpty

@Component
@ConditionalOnProperty("app.tribe.api-key")
class TribeClient(
  defaultWebClientBuilder: WebClient.Builder,
  private val tribeConfigurationProperties: TribeConfigurationProperties
) {
  private val webClientBuilder = defaultWebClientBuilder.clone()
    .defaultHeader("X-Api-Key", tribeConfigurationProperties.apiKey)
    .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)

  fun listPeople(
    selectors: Map<@NotEmpty String, @NotEmpty String> = mapOf(),
    fieldFilters: Set<@NotEmpty String> = setOf()
  ): Flux<ListedPerson> {
    val baseUrlBuilder = UriComponentsBuilder
      .fromHttpUrl("https://${tribeConfigurationProperties.host}")
      .path("/api/v1/people-attributes")

    val endpoint = selectors.entries
      .fold(baseUrlBuilder) { builder, e ->
        builder.queryParam(e.key, e.value)
      }
      .let {
        if (fieldFilters.isNotEmpty()) it.queryParam("fields", fieldFilters) else it
      }
      .build()
      .toUriString()

    val webClient = webClientBuilder.clone()
      .baseUrl(endpoint)
      .build()

    return webClient.get()
      .retrieve()
      .bodyToFlux(ListedPerson::class.java)
  }

  fun getPerson(id: Int): Mono<Person> {
    val endpoint = UriComponentsBuilder
      .fromHttpUrl("https://${tribeConfigurationProperties.host}")
      .path("/api/v1/people/{id}")
      .buildAndExpand(mapOf("id" to id))
      .toUriString()

    val webClient = webClientBuilder.clone()
      .baseUrl(endpoint)
      .build()

    return webClient.get()
      .retrieve()
      .bodyToMono(Person::class.java)
  }

  fun getAsset(id: Int): Pair<String, Mono<ByteArray>> {
    val endpoint = UriComponentsBuilder
      .fromHttpUrl("https://${tribeConfigurationProperties.host}")
      .path("/api/v1/attributes/{id}/asset")
      .buildAndExpand(mapOf("id" to id))
      .toUriString()

    val webClient = webClientBuilder.clone()
      .baseUrl(endpoint)
      .build()

    return endpoint to webClient.get()
      .retrieve()
      .bodyToMono(ByteArrayResource::class.java)
      .map { it.byteArray }
  }
}
