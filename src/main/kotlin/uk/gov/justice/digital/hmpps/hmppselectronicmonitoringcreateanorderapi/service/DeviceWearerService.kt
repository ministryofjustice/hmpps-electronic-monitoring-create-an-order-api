package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateDeviceWearerDto
import java.util.*

@Service
class DeviceWearerService(
  val repo: DeviceWearerRepository,
) {

  fun getDeviceWearer(orderId: UUID, username: String): DeviceWearer {
    return repo.findByOrderIdAndOrderUsername(
      orderId,
      username,
    ).orElseThrow {
      EntityNotFoundException("Device Wearer for $orderId not found")
    }
  }

  fun updateDeviceWearer(
    orderId: UUID,
    username: String,
    deviceWearerUpdateRecord: UpdateDeviceWearerDto,
  ): DeviceWearer {
    val deviceWearer = repo.findByOrderIdAndOrderUsernameAndOrderStatus(
      orderId,
      username,
      FormStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("Device Wearer for $orderId not found")
    }

    with(deviceWearerUpdateRecord) {
      deviceWearer.nomisId = nomisId
      deviceWearer.pncId = pncId
      deviceWearer.deliusId = deliusId
      deviceWearer.prisonNumber = prisonNumber
      deviceWearer.firstName = firstName
      deviceWearer.lastName = lastName
      deviceWearer.alias = alias
      deviceWearer.adultAtTimeOfInstallation = adultAtTimeOfInstallation
      deviceWearer.gender = gender
      deviceWearer.dateOfBirth = dateOfBirth
    }

    return repo.save(deviceWearer)
  }
}
