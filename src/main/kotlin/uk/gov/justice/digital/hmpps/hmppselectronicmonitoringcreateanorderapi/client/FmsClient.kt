package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import java.util.UUID

@Service
class FmsClient(
  @Value("\${services.serco.url}") url: String,
  private val fmsAuthClient: FmsAuthClient,
  private val objectMapper: ObjectMapper,
) {
  private val webClient: WebClient = WebClient.builder().baseUrl(url).build()
  fun createDeviceWearer(deviceWearer: DeviceWearer, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/device_wearer/createDW")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(deviceWearer)
      .retrieve()
      .onStatus({ t -> t.is5xxServerError }, {
        it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
          Mono.error(
            CreateSercoEntityException(
              "Error creating FMS Device Wearer for order: $orderId with error: ${error?.error?.detail}",
            ),
          )
        }
      })
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun createMonitoringOrder(order: MonitoringOrder, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/monitoring_order/createMO")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(order)
      .retrieve()
      .onStatus({ t -> t.is5xxServerError }, {
        it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
          Mono.error(
            CreateSercoEntityException(
              "Error creating FMS Monitoring Order for order: $orderId with error: ${error?.error?.detail}",
            ),
          )
        }
      })
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }
}
