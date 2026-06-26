package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.projections.DeviceWearerListInformation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.projections.InterestedPartiesListInformation
import java.util.UUID

data class OrderInformationDto(
  val id: UUID,
  val status: OrderStatus,
  val type: RequestType,
  val deviceWearer: DeviceWearerListInformation?,
  val interestedParties: InterestedPartiesListInformation?,
)
