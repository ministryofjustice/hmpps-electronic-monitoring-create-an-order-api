package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateResponsibleAdultDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.ResponsibleAdultRepository
import java.util.UUID

@Service
class ResponsibleAdultService(
  val responsibleAdultRepo: ResponsibleAdultRepository,
) : OrderSectionServiceBase() {
  fun getResponsibleAdult(orderId: UUID, username: String): ResponsibleAdult {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    // Find an existing responsible adult or create a new responsible adult
    return responsibleAdultRepo.findByOrderIdAndOrderUsernameAndOrderStatus(
      order.id,
      order.username,
      order.status,
    ).orElse(
      ResponsibleAdult(
        orderId = orderId,
      ),
    )
  }

  fun createOrUpdateResponsibleAdult(
    orderId: UUID,
    username: String,
    deviceWearerResponsibleAdultUpdateRecord: UpdateResponsibleAdultDto,
  ): ResponsibleAdult {
    val responsibleAdult = this.getResponsibleAdult(
      orderId,
      username,
    )

    with(deviceWearerResponsibleAdultUpdateRecord) {
      responsibleAdult.fullName = fullName
      responsibleAdult.relationship = relationship
      responsibleAdult.otherRelationshipDetails = otherRelationshipDetails
      responsibleAdult.contactNumber = contactNumber
    }

    return responsibleAdultRepo.save(responsibleAdult)
  }
}
