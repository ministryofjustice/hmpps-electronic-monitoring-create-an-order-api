package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import java.util.UUID

@Repository
interface OrderVersionRepository : JpaRepository<OrderVersion, UUID> {
  @Transactional
  @Modifying
  @Query("UPDATE OrderVersion version SET version.fmsCaseState = :caseState WHERE version.id = :versionId")
  fun updateFmsCaseState(@Param("versionId") versionId: UUID, @Param("caseState") caseState: CaseState): Int
}
