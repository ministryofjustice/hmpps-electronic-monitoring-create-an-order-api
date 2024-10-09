package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.EnformenceZoneRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import java.util.*

class EnforcementZoneService(
  val repo: EnformenceZoneRepository,
  val webClient: DocumentApiClient,
  val orderRepo: OrderFormRepository,
) {

  val allowedFileExtensions: List<String> = listOf("pdf", "jpeg")

  fun updateEnforcementZone(orderId: UUID, username: String, enforcementZone: EnforcementZoneConditions, multipartFile: MultipartFile) {
    val order = orderRepo.findByIdAndUsernameAndStatus(
      orderId,
      username,
      FormStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("An editable order with $orderId does not exist")
    }
  }
}
