package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity
import reactor.core.publisher.Flux
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.DocumentApiBadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentUploadResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.Duration

@Component
class DocumentApiClient(private val documentManagementApiWebClient: WebClient) {
  companion object {

    const val DOCUMENT_MANAGEMENT_API_SERVICE_NAME_HEADER = "Service-Name"
    const val SERVICE_NAME_HEADER = "create-electronic-monitoring-order"
  }
  fun createDocument(documentUuid: String, multipartBodyBuilder: MultipartBodyBuilder): DocumentUploadResponse? =
    documentManagementApiWebClient
      .post()
      .uri("/documents/CEMO_ATTACHMENT/$documentUuid")
      .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
      .header(DOCUMENT_MANAGEMENT_API_SERVICE_NAME_HEADER, SERVICE_NAME_HEADER)
      .retrieve()
      .onStatus(
        { status -> status === HttpStatus.BAD_REQUEST },
        { clientResponse ->
          clientResponse.bodyToMono(ErrorResponse::class.java).map { error ->
            DocumentApiBadRequestException(error)
          }
        },
      )
      .bodyToMono(DocumentUploadResponse::class.java)
      .block()

  fun getDocument(documentUuid: String): ResponseEntity<Flux<InputStreamResource>>? = documentManagementApiWebClient
    .get()
    .uri("/documents/$documentUuid/file")
    .accept(MediaType.ALL)
    .header(DOCUMENT_MANAGEMENT_API_SERVICE_NAME_HEADER, SERVICE_NAME_HEADER)
    .retrieve()
    .onStatus(
      { status -> status === HttpStatus.BAD_REQUEST },
      { clientResponse ->
        clientResponse.bodyToMono(ErrorResponse::class.java).map { error ->
          DocumentApiBadRequestException(error)
        }
      },
    )
    .toEntityFlux(InputStreamResource::class.java)
    .retryWhen(Retry.fixedDelay(1, Duration.ofMillis(250)))
    .block()

  fun deleteDocument(documentUuid: String): ResponseEntity<Any>? = documentManagementApiWebClient
    .delete()
    .uri("/documents/$documentUuid")
    .header(DOCUMENT_MANAGEMENT_API_SERVICE_NAME_HEADER, SERVICE_NAME_HEADER)
    .retrieve()
    .toEntity<Any>()
    .block()
}
