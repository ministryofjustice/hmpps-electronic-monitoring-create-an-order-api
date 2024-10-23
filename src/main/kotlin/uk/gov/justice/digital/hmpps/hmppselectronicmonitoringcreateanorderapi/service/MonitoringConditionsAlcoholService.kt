package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MonitoringConditionsAlcoholRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateAlcoholMonitoringConditionsDto
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class MonitoringConditionsAlcoholService(
  val orderRepo: OrderRepository,
  val alcoholMonitoringConditionsRepo: MonitoringConditionsAlcoholRepository,
  val addressRepo: AddressRepository,
) {
  fun getAlcoholMonitoringConditions(orderId: UUID, username: String): AlcoholMonitoringConditions {
    // Verify the order belongs to the user and is in draft state
    val order = orderRepo.findByIdAndUsernameAndStatus(
      orderId,
      username,
      OrderStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("An editable order with $orderId does not exist")
    }

    // Find existing alcohol monitoring conditions or create new alcohol monitoring conditions
    return order.monitoringConditionsAlcohol ?: AlcoholMonitoringConditions(orderId = orderId)
  }

  fun createOrUpdateAlcoholMonitoringConditions(
    orderId: UUID,
    username: String,
    alcoholMonitoringConditionsUpdateRecord: UpdateAlcoholMonitoringConditionsDto,
  ): AlcoholMonitoringConditions {
    val alcoholMonitoringConditions = this.getAlcoholMonitoringConditions(
      orderId,
      username,
    )

    var alcoholMonitoringAddressId: UUID? = null

    try {
      val alcoholMonitoringInstallationAddressType: AddressType = AddressType.valueOf(alcoholMonitoringConditionsUpdateRecord.installationLocation.toString())

      alcoholMonitoringAddressId = addressRepo.findByOrderIdAndOrderUsernameAndOrderStatusAndAddressType(
        orderId,
        username,
        OrderStatus.IN_PROGRESS,
        alcoholMonitoringInstallationAddressType,
      ).getOrNull()?.id
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

    return alcoholMonitoringConditionsRepo.save(alcoholMonitoringConditions)
  }
}
