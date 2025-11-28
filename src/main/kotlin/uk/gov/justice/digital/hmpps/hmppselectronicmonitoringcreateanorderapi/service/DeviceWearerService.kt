package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDeviceWearerDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateIdentityNumbersDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateNoFixedAbodeDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.util.*

@Service
class DeviceWearerService : OrderSectionServiceBase() {
  fun updateDeviceWearer(orderId: UUID, username: String, updateRecord: UpdateDeviceWearerDto): DeviceWearer {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    order.deviceWearer = DeviceWearer(
      versionId = order.getCurrentVersion().id,
      firstName = updateRecord.firstName?.trim(),
      lastName = updateRecord.lastName?.trim(),
      alias = updateRecord.alias,
      adultAtTimeOfInstallation = updateRecord.adultAtTimeOfInstallation,
      sex = updateRecord.sex.toString(),
      gender = updateRecord.gender,
      dateOfBirth = updateRecord.dateOfBirth,
      disabilities = updateRecord.disabilities,
      otherDisability = updateRecord.otherDisability,
      language = updateRecord.language,
      interpreterRequired = updateRecord.interpreterRequired,
      noFixedAbode = order.deviceWearer?.noFixedAbode,
      nomisId = order.deviceWearer?.nomisId,
      deliusId = order.deviceWearer?.deliusId,
      pncId = order.deviceWearer?.pncId,
      prisonNumber = order.deviceWearer?.prisonNumber,
      homeOfficeReferenceNumber = order.deviceWearer?.homeOfficeReferenceNumber,
    )

    // Clear responsible adult when device wearer is adult
    if (updateRecord.adultAtTimeOfInstallation == true) {
      order.deviceWearerResponsibleAdult = null
    }

    return orderRepo.save(order).deviceWearer!!
  }

  fun updateNoFixedAbode(orderId: UUID, username: String, updateRecord: UpdateNoFixedAbodeDto): DeviceWearer {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    if (order.deviceWearer === null) {
      throw EntityNotFoundException("An editable device wearer for $orderId could not be found")
    }

    order.deviceWearer?.noFixedAbode = updateRecord.noFixedAbode

    if (updateRecord.noFixedAbode == true) {
      val personalAddressTypes = setOf(
        AddressType.PRIMARY,
        AddressType.SECONDARY,
        AddressType.TERTIARY,
      )
      order.addresses.removeAll { it.addressType in personalAddressTypes }
    }

    orderRepo.save(order)

    return order.deviceWearer!!
  }

  fun updateIdentityNumbers(orderId: UUID, username: String, updateRecord: UpdateIdentityNumbersDto): DeviceWearer {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    if (order.deviceWearer === null) {
      throw EntityNotFoundException("An editable device wearer for $orderId could not be found")
    }

    order.deviceWearer?.nomisId = updateRecord.nomisId?.trim()
    order.deviceWearer?.pncId = updateRecord.pncId?.trim()
    order.deviceWearer?.deliusId = updateRecord.deliusId?.trim()
    order.deviceWearer?.prisonNumber = updateRecord.prisonNumber?.trim()
    order.deviceWearer?.homeOfficeReferenceNumber = updateRecord.homeOfficeReferenceNumber?.trim()

    orderRepo.save(order)

    return order.deviceWearer!!
  }
}
