package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.projections

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.time.OffsetDateTime
import java.util.UUID

interface OrderVersionListInformation {
  fun getId(): UUID
  fun getVersionId(): UUID
  fun getType(): RequestType
  fun getStatus(): OrderStatus
  fun getFirstName(): String?
  fun getLastName(): String?
  fun getNotifyingOrganisation(): String?
  fun getLastUpdatedBy(): String?
  fun getLastUpdateDateTime(): OffsetDateTime?
}
