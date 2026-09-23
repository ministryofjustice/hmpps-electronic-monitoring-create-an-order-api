package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.AddStatusUpdateService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.StatusUpdateReasonRequest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.UpdateOrderStatusService

@Service
class ReturnsEventProcessor(
  private val updateOrderStatus: UpdateOrderStatusService,
  private val addStatusUpdate: AddStatusUpdateService,
) {
  fun onRejected(message: ReturnMessage) {
    updateOrderStatus.execute(message.caseId, OrderStatus.REJECTED)
    addStatusUpdate.execute(
      message.caseId,
      ProcessingStatus.REJECTED,
      message.datetimeOfStatusChange,
      message.reasons.map { StatusUpdateReasonRequest(section = it.section, details = it.details) },
    )
  }
}
