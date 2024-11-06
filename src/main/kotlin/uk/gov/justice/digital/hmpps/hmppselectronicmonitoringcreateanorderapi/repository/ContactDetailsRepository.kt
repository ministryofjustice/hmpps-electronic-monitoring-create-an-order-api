package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import java.util.*

@Repository
interface ContactDetailsRepository : JpaRepository<ContactDetails, UUID> {
  fun findByOrderIdAndOrderUsername(id: UUID, username: String): Optional<ContactDetails>

  fun findByOrderIdAndOrderUsernameAndOrderStatus(
    id: UUID,
    username: String,
    status: OrderStatus,
  ): Optional<ContactDetails>
}
