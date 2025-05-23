package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateResponsibleAdultDto
import java.util.UUID

@Service
class ResponsibleAdultService : OrderSectionServiceBase() {
  fun updateResponsibleAdult(
    orderId: UUID,
    username: String,
    updateRecord: UpdateResponsibleAdultDto,
  ): ResponsibleAdult {
    val order = this.findEditableOrder(orderId, username)

    order.deviceWearerResponsibleAdult = ResponsibleAdult(
      versionId = order.getCurrentVersion().id,
      fullName = updateRecord.fullName,
      relationship = updateRecord.relationship,
      otherRelationshipDetails = updateRecord.otherRelationshipDetails,
      contactNumber = updateRecord.contactNumber,
    )

    return orderRepo.save(order).deviceWearerResponsibleAdult!!
  }
}
