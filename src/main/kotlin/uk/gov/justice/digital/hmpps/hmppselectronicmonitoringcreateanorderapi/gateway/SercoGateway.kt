package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.gateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.SercoAuthClient

@Service
class SercoGateway(
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
