package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import java.util.*

@Repository
interface MonitoringConditionsRepository : JpaRepository<MonitoringConditions, UUID> {
  fun findByOrderIdAndOrderUsernameAndOrderStatus(
    id: UUID,
    username: String,
    status: FormStatus,
  ): Optional<MonitoringConditions>
}
