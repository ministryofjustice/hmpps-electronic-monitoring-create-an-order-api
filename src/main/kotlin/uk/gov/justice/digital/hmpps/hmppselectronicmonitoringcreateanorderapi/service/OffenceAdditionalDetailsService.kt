package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OffenceAdditionalDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceAdditionalDetailsDto
import java.util.UUID

@Service
class OffenceAdditionalDetailsService : OrderSectionServiceBase() {

  fun updateOffenceAdditionalDetails(
    orderId: UUID,
    username: String,
    dto: UpdateOffenceAdditionalDetailsDto,
  ): OffenceAdditionalDetails {
    val order = findEditableOrder(orderId, username)

    val existingId = order.offenceAdditionalDetails?.id
    val idToUse = dto.id ?: existingId ?: UUID.randomUUID()

    order.offenceAdditionalDetails = OffenceAdditionalDetails(
      id = idToUse,
      versionId = order.getCurrentVersion().id,
      additionalDetails = dto.additionalDetails,
    )

    return orderRepo.save(order).offenceAdditionalDetails!!
  }
}
