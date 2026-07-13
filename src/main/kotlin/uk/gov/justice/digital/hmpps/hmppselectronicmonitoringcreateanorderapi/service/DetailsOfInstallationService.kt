package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DetailsOfInstallation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDetailsOfInstallationDto
import java.util.UUID

@Service
class DetailsOfInstallationService : OrderSectionServiceBase() {
  fun updateDetailsOfInstallation(
    orderId: UUID,
    username: String,
    dto: UpdateDetailsOfInstallationDto,
  ): DetailsOfInstallation {
    val order = this.findEditableOrder(orderId, username)

    order.detailsOfInstallation =
      DetailsOfInstallation(
        versionId = order.versionId,
        riskCategory = dto.riskCategory,
        riskDetails = dto.riskDetails,
        genderRiskDetails = dto.genderRiskDetails,
      )

    return updateLastUpdatedByAndSaveOrder(order).detailsOfInstallation!!
  }
}
