package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerRepository
import java.time.LocalDate
import java.util.UUID

@Service
class DeviceWearerService(
  val repo: DeviceWearerRepository,
) {

  fun createDeviceWearer(orderId: UUID, firstName: String? = null, lastName: String? = null, gender: String? = null, dateOfBirth: LocalDate? = null): DeviceWearer {
    val deviceWearer = DeviceWearer(orderId = orderId, firstName = firstName, lastName = lastName, gender = gender, dateOfBirth = dateOfBirth)
    repo.save(deviceWearer)
    return deviceWearer
  }
}
