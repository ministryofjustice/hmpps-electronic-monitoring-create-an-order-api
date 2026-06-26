package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.projections

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.util.UUID

interface OrderVersionListInformation {
  fun getId(): UUID
  fun getType(): RequestType
  fun getStatus(): OrderStatus
  fun getDeviceWearer(): DeviceWearerListInformation?
  fun getInterestedParties(): InterestedPartiesListInformation?
}
