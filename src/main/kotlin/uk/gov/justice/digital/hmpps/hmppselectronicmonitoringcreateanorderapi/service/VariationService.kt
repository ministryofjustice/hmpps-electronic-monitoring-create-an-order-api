package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateVariationDetailsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import java.time.ZonedDateTime
import java.util.*

@Service
class VariationService : OrderSectionServiceBase() {
  fun updateVariationDetails(
    orderId: UUID,
    username: String,
    updateRecord: UpdateVariationDetailsDto,
  ): VariationDetails {
    val order = this.findEditableOrder(orderId, username)

    with(updateRecord) {
      order.variationDetails = VariationDetails(
        versionId = order.getCurrentVersion().id,
        variationType = VariationType.valueOf(variationType),
        variationDate = ZonedDateTime.parse(variationDate),
      )
    }

    return orderRepo.save(order).variationDetails!!
  }
}
