package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoDeviceWearerException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.SercoResponse
import java.util.UUID

@Service
class SercoClient(
  @Value("\${services.serco.url}") url: String,
  private val sercoAuthClient: SercoAuthClient,
  private val objectMapper: ObjectMapper,
) {
  private val webClient: WebClient = WebClient.builder().baseUrl(url).build()
  fun createDeviceWeaer(deviceWearer: DeviceWearer, orderId: UUID): SercoResponse {
    val token = sercoAuthClient.getClientToken()
    val result = webClient.post().uri("/device_wearer/createDW")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(deviceWearer)
      .retrieve()
      .onStatus({ t -> t.is5xxServerError }, { Mono.error(CreateSercoDeviceWearerException("Error creating Serco Device Wearer for order: $orderId")) })
      .bodyToMono(SercoResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }
}
