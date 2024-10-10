package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddressInformation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerAddressInformationRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateDeviceWearerAddressDto
import java.util.UUID

@Service
class DeviceWearerAddressInformationService(
  val repo: DeviceWearerAddressInformationRepository,
) {

  fun updateAddress(
    orderId: UUID,
    username: String,
    deviceWearerAddressUpdateRecord: UpdateDeviceWearerAddressDto,
  ): DeviceWearerAddressInformation {
    val addressInformation = repo.findByOrderIdAndOrderUsernameAndOrderStatus(
      orderId,
      username,
      OrderStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("An editable address does not exist for order: $orderId")
    }

    with(deviceWearerAddressUpdateRecord) {
      val address = Address(
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        addressLine3 = addressLine3,
        addressLine4 = addressLine4,
        postcode = postcode,
      )

      if (addressType === DeviceWearerAddressType.PRIMARY) {
        addressInformation.primaryAddress = address
      }

      if (addressType === DeviceWearerAddressType.SECONDARY) {
        addressInformation.secondaryAddress = address
      }

      if (addressType === DeviceWearerAddressType.TERTIARY) {
        addressInformation.tertiaryAddress = address
      }
    }

    return repo.save(addressInformation)
  }
}
