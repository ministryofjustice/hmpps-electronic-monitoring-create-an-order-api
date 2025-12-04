package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.time.OffsetDateTime
import java.util.UUID

data class VersionInformationDTO(
  val orderId: UUID,
  val versionId: UUID,
  val versionNumber: Int,
  val fmsResultDate: OffsetDateTime?,
  val submittedBy: String?,
  val type: RequestType,
)
