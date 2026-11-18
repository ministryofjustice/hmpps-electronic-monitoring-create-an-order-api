package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.StatusUpdate
import java.util.UUID

@Repository
interface StatusUpdateRepository : JpaRepository<StatusUpdate, UUID> {
  fun findAllByOrderId(orderId: UUID): List<StatusUpdate>
}
