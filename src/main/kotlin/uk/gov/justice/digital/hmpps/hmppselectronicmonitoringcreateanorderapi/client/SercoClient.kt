package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class SercoClient(
  @Value("\${services.serco.url}") url: String,
  private val sercoAuthClient: SercoAuthClient,
) {
  private val webClient: WebClient = WebClient.builder().baseUrl(url).build()
  fun createDeviceWeaer(): String {
    val token = sercoAuthClient.getClientToken()

    val result = webClient.post().uri("/device_wearer/createDW")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .retrieve()
      .bodyToMono(String::class.java)
      .block()!!
    return result
  }
}
