package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.util.UUID

data class OrderInformationDto(
  val id: UUID,
  val versionId: UUID,
  val status: OrderStatus,
  val type: RequestType,
  val firstName: String?,
  val lastName: String?,
  val notifyingOrganisation: String?,
)
