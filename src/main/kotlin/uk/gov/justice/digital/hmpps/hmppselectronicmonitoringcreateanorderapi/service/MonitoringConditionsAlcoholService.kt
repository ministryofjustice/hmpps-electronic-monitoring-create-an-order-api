package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateAlcoholMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.util.*

@Service
class MonitoringConditionsAlcoholService : OrderSectionServiceBase() {
  fun createOrUpdateAlcoholMonitoringConditions(
    orderId: UUID,
    username: String,
    alcoholMonitoringConditionsUpdateRecord: UpdateAlcoholMonitoringConditionsDto,
  ): AlcoholMonitoringConditions {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    // Find existing alcohol monitoring conditions or create new alcohol monitoring conditions
    val alcoholMonitoringConditions =
      order.monitoringConditionsAlcohol ?: AlcoholMonitoringConditions(orderId = orderId)

    // Add relevant address ID when installation location is an address
    var alcoholMonitoringAddressId: UUID? = null
    try {
      val alcoholMonitoringInstallationAddressType: AddressType = AddressType.valueOf(
        alcoholMonitoringConditionsUpdateRecord.installationLocation.toString(),
      )
      alcoholMonitoringAddressId = order.addresses.find {
        it.addressType == alcoholMonitoringInstallationAddressType
      }?.id
    } catch (_: IllegalArgumentException) {
    }

    with(alcoholMonitoringConditionsUpdateRecord) {
      alcoholMonitoringConditions.monitoringType = monitoringType
      alcoholMonitoringConditions.startDate = startDate
      alcoholMonitoringConditions.endDate = endDate
      alcoholMonitoringConditions.installationLocation = installationLocation
      alcoholMonitoringConditions.prisonName = prisonName
      alcoholMonitoringConditions.probationOfficeName = probationOfficeName
      alcoholMonitoringConditions.installationAddressId = alcoholMonitoringAddressId
    }

    order.monitoringConditionsAlcohol = alcoholMonitoringConditions

    return orderRepo.save(order).monitoringConditionsAlcohol!!
  }
}
