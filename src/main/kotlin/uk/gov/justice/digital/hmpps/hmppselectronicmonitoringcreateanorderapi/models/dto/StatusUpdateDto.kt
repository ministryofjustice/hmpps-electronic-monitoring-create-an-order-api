package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.StatusUpdate
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.time.ZonedDateTime
import java.util.UUID

data class StatusUpdateDto(
  val id: UUID,
  val orderId: UUID,
  val orderVersionId: UUID,
  val orderVersionType: RequestType,
  val status: ProcessingStatus,
  val datetimeOfStatusChange: ZonedDateTime,
  val statusUpdatedBy: String?,
  val reasons: List<StatusUpdateReasonDto>,
) {
  companion object {
    fun from(statusUpdate: StatusUpdate) = StatusUpdateDto(
      id = statusUpdate.id,
      orderId = statusUpdate.orderId,
      orderVersionId = statusUpdate.orderVersionId,
      orderVersionType = statusUpdate.orderVersionType,
      status = statusUpdate.status,
      datetimeOfStatusChange = statusUpdate.datetimeOfStatusChange,
      statusUpdatedBy = statusUpdate.statusUpdatedBy,
      reasons = statusUpdate.statusUpdateReasons.map { StatusUpdateReasonDto(it.section, it.details) },
    )
  }
}

data class StatusUpdateReasonDto(val section: String, val details: String?)
