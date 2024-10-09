package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import java.util.*

@Repository
interface DeviceWearerAddressRepository : JpaRepository<DeviceWearerAddress, UUID> {
  fun findByOrderIdAndOrderUsernameAndOrderStatusAndAddressType(
    id: UUID,
    username: String,
    status: FormStatus,
    addressType: DeviceWearerAddressType,
  ): Optional<DeviceWearerAddress>
}
