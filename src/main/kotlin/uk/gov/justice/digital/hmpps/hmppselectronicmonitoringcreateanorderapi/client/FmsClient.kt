package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import java.util.*

@Service
class FmsClient(
  @Value("\${services.serco.url}") url: String,
  private val fmsAuthClient: FmsAuthClient,
  private val objectMapper: ObjectMapper,
) {
  private val webClient: WebClient = WebClient.builder().baseUrl(url).build()

  fun createDeviceWearer(deviceWearer: DeviceWearer, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/x_seem_cemo/device_wearer/createDW")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(deviceWearer)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error creating FMS Device Wearer for order: $orderId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun createMonitoringOrder(order: MonitoringOrder, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/x_seem_cemo/monitoring_order/createMO")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(order)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error creating FMS Monitoring Order for order: $orderId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun updateDeviceWearer(deviceWearer: DeviceWearer, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/x_seem_cemo/device_wearer/updateDW")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(deviceWearer)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error updating FMS Device Wearer for order: $orderId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun updateMonitoringOrder(order: MonitoringOrder, orderId: UUID): FmsResponse {
    val token = fmsAuthClient.getClientToken()
    val result = webClient.post().uri("/x_seem_cemo/monitoring_order/updateMO")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(order)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error updating FMS Monitoring Order for order: $orderId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }

  fun createAttachment(
    fileName: String,
    caseId: String,
    file: InputStreamResource,
    documentType: String,
  ): FmsAttachmentResponse {
    val token = fmsAuthClient.getClientToken()
    val tableName = "x_serg2_ems_csm_sr_mo_new"

    val result = webClient.post()
      .uri { uriBuilder ->
        uriBuilder
          .path("/now/v1/attachment_csm/file")
          .queryParam("table_name", tableName)
          .queryParam("table_sys_id", caseId)
          .queryParam("file_name", fileName)
          .build()
      }
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .contentType(MediaType.APPLICATION_OCTET_STREAM) // turns into binary
      .bodyValue(file)
      .retrieve()
      .onStatus(
        { t -> t.isError },
        {
          it.bodyToMono(FmsErrorResponse::class.java).flatMap { error ->
            Mono.error(
              CreateSercoEntityException(
                "Error creating $documentType attachment for order: $caseId with error: ${error?.error?.detail}",
              ),
            )
          }
        },
      )
      .bodyToMono(FmsAttachmentResponse::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }
}
