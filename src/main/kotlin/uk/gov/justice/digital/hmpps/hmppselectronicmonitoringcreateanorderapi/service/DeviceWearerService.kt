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

    val existingDeviceWearer = order.deviceWearer
    if (existingDeviceWearer != null) {
      existingDeviceWearer.firstName = updateRecord.firstName?.trim()
      existingDeviceWearer.middleName = updateRecord.middleName?.trim()
      existingDeviceWearer.lastName = updateRecord.lastName?.trim()
      existingDeviceWearer.alias = updateRecord.alias
      existingDeviceWearer.adultAtTimeOfInstallation = updateRecord.adultAtTimeOfInstallation
      existingDeviceWearer.sex = updateRecord.sex.toString()
      existingDeviceWearer.gender = updateRecord.gender
      existingDeviceWearer.dateOfBirth = updateRecord.dateOfBirth
      existingDeviceWearer.disabilities = updateRecord.disabilities
      existingDeviceWearer.otherDisability = updateRecord.otherDisability
      existingDeviceWearer.language = updateRecord.language
      existingDeviceWearer.interpreterRequired = updateRecord.interpreterRequired
    } else {
      order.deviceWearer = DeviceWearer(
        versionId = order.getCurrentVersion().id,
        firstName = updateRecord.firstName?.trim(),
        middleName = updateRecord.middleName?.trim(),
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

      )
    }

    // Clear responsible adult when device wearer is adult
    if (updateRecord.adultAtTimeOfInstallation == true) {
      order.deviceWearerResponsibleAdult = null
    }

    return updateLastUpdatedByAndSaveOrder(order).deviceWearer!!
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

    updateLastUpdatedByAndSaveOrder(order)

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

    order.deviceWearer?.complianceAndEnforcementPersonReference =
      updateRecord.complianceAndEnforcementPersonReference?.trim()
    order.deviceWearer?.courtCaseReferenceNumber = updateRecord.courtCaseReferenceNumber?.trim()

    updateLastUpdatedByAndSaveOrder(order)

    return order.deviceWearer!!
  }
}
