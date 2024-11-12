package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import java.util.*

@Service
class InstallationAndRiskService() : OrderSectionServiceBase() {
  fun updateInstallationAndRisk(
    orderId: UUID,
    username: String,
    installationAndRisk: InstallationAndRisk,
  ): InstallationAndRisk {
    val order = findEditableOrder(orderId, username)
    installationAndRisk.orderId = order.id
    order.installationAndRisk = installationAndRisk
    return orderRepo.save(order).installationAndRisk!!
  }
}
