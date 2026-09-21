package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.UpdateOrderStatusService

@Service
class ReturnsEventProcessor(private val updateOrderStatusService: UpdateOrderStatusService) {
  fun onRejected(message: ReturnMessage) {
    updateOrderStatusService.execute(message.caseId, OrderStatus.REJECTED)
  }
}
