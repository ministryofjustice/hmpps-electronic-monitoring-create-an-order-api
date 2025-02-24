package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationAndRiskDto
import java.util.*

@Service
class InstallationAndRiskService : OrderSectionServiceBase() {
  fun updateInstallationAndRisk(
    orderId: UUID,
    username: String,
    updateRecord: UpdateInstallationAndRiskDto,
  ): InstallationAndRisk {
    val order = findEditableOrder(orderId, username)

    order.installationAndRisk = InstallationAndRisk(
      versionId = order.getCurrentVersion().id,
      offence = updateRecord.offence,
      riskCategory = updateRecord.riskCategory,
      riskDetails = updateRecord.riskDetails,
      mappaLevel = updateRecord.mappaLevel,
      mappaCaseType = updateRecord.mappaCaseType,
    )

    return orderRepo.save(order).installationAndRisk!!
  }
}
