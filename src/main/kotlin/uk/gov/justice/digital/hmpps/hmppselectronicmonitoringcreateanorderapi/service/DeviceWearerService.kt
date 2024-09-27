package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import java.time.LocalDate
import java.util.*

@Service
class DeviceWearerService(
  val deviceWearerRepo: DeviceWearerRepository,
  val orderFormRepo: OrderFormRepository,
) {

  fun createDeviceWearer(
    orderId: UUID,
    firstName: String? = null,
    lastName: String? = null,
    alias: String? = null,
    gender: String? = null,
    dateOfBirth: LocalDate? = null,
  ): DeviceWearer {
    val deviceWearer = DeviceWearer(
      orderId = orderId,
      firstName = firstName,
      lastName = lastName,
      alias = alias,
      gender = gender,
      dateOfBirth = dateOfBirth,
    )

    deviceWearerRepo.save(deviceWearer)
    return deviceWearer
  }

  fun getDeviceWearer(username: String, orderId: UUID): DeviceWearer? {
    orderFormRepo.findByUsernameAndId(username, orderId)
      .orElseThrow { NoSuchElementException("Order could not be found.") }

    return deviceWearerRepo.findByOrderId(orderId)
      .orElseThrow { NoSuchElementException("Device wearer could not be found.") }
  }

  fun updateDeviceWearer(
    username: String,
    orderId: UUID,
    firstName: String? = null,
    lastName: String? = null,
    alias: String? = null,
    gender: String? = null,
    dateOfBirth: LocalDate? = null,
  ): DeviceWearer? {
    orderFormRepo.findByUsernameAndId(username, orderId)
      .orElseThrow { NoSuchElementException("Order could not be found.") }

    val deviceWearer = deviceWearerRepo.findByOrderId(orderId)
      .orElseThrow { NoSuchElementException("Device wearer could not be found.") }

    deviceWearer.firstName = firstName
    deviceWearer.lastName = lastName
    deviceWearer.alias = alias
    deviceWearer.gender = gender
    deviceWearer.dateOfBirth = dateOfBirth

    return deviceWearerRepo.save(deviceWearer)
  }
}
