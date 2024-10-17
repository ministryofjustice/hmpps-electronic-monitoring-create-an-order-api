package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateDeviceWearerDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateNoFixedAbodeDto
import java.util.*

@Service
class DeviceWearerService(
  val repo: DeviceWearerRepository,
) {
  fun updateDeviceWearer(
    orderId: UUID,
    username: String,
    deviceWearerUpdateRecord: UpdateDeviceWearerDto,
  ): DeviceWearer {
    val deviceWearer = repo.findByOrderIdAndOrderUsernameAndOrderStatus(
      orderId,
      username,
      OrderStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("An editable device wearer for $orderId could not be found")
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
      deviceWearer.sex = sex
      deviceWearer.gender = gender
      deviceWearer.dateOfBirth = dateOfBirth
      deviceWearer.disabilities = disabilities
    }

    return repo.save(deviceWearer)
  }

  fun updateNoFixedAbode(
    orderId: UUID,
    username: String,
    updateRecord: UpdateNoFixedAbodeDto,
  ): DeviceWearer {
    val deviceWearer = repo.findByOrderIdAndOrderUsernameAndOrderStatus(
      orderId,
      username,
      OrderStatus.IN_PROGRESS,
    ).orElseThrow {
      EntityNotFoundException("An editable device wearer for $orderId could not be found")
    }

    with(updateRecord) {
      deviceWearer.noFixedAbode = noFixedAbode
    }

    return repo.save(deviceWearer)
  }
}
