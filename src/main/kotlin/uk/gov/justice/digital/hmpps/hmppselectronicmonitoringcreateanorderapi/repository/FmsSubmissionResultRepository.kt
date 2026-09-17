package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import java.util.*

interface FmsSubmissionResultRepository : JpaRepository<FmsSubmissionResult, UUID> {
  @Query(
    """
    SELECT f FROM FmsSubmissionResult f
    WHERE f.deviceWearerResult.deviceWearerId = :caseId
    """,
  )
  fun findByCaseId(@Param("caseId") caseId: String): FmsSubmissionResult?
}
