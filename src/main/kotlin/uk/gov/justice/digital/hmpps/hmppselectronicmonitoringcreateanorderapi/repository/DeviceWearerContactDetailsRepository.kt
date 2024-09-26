package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import java.util.*

@Repository
interface DeviceWearerContactDetailsRepository : JpaRepository<DeviceWearerContactDetails, UUID> {
  fun findByOrderIdAndOrderUsername(id: UUID, username: String): Optional<DeviceWearerContactDetails>
}
