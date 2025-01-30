package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDeviceWearerDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateIdentityNumbersDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateNoFixedAbodeDto
import java.util.*

@Service
class DeviceWearerService() : OrderSectionServiceBase() {
  fun updateDeviceWearer(orderId: UUID, username: String, updateRecord: UpdateDeviceWearerDto): DeviceWearer {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    val deviceWearer = DeviceWearer(
      orderId = orderId,
      firstName = updateRecord.firstName,
      lastName = updateRecord.lastName,
      alias = updateRecord.alias,
      adultAtTimeOfInstallation = updateRecord.adultAtTimeOfInstallation,
      sex = updateRecord.sex,
      gender = updateRecord.gender,
      dateOfBirth = updateRecord.dateOfBirth,
      disabilities = updateRecord.disabilities,
      language = updateRecord.language,
      interpreterRequired = updateRecord.interpreterRequired,
    )

    order.deviceWearer = deviceWearer
    orderRepo.save(order)

    return deviceWearer
  }

  fun updateNoFixedAbode(orderId: UUID, username: String, updateRecord: UpdateNoFixedAbodeDto): DeviceWearer {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    if (order.deviceWearer === null) {
      throw EntityNotFoundException("An editable device wearer for $orderId could not be found")
    }

    order.deviceWearer?.noFixedAbode = updateRecord.noFixedAbode

    orderRepo.save(order)

    return order.deviceWearer!!
  }

  fun updateIdentityNumbers(orderId: UUID, username: String, updateRecord: UpdateIdentityNumbersDto): DeviceWearer {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    if (order.deviceWearer === null) {
      throw EntityNotFoundException("An editable device wearer for $orderId could not be found")
    }

    order.deviceWearer?.nomisId = updateRecord.nomisId
    order.deviceWearer?.pncId = updateRecord.pncId
    order.deviceWearer?.deliusId = updateRecord.deliusId
    order.deviceWearer?.prisonNumber = updateRecord.prisonNumber
    order.deviceWearer?.homeOfficeReferenceNumber = updateRecord.homeOfficeReferenceNumber

    orderRepo.save(order)

    return order.deviceWearer!!
  }
}
