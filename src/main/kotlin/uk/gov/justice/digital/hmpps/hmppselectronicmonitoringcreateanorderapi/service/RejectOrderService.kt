package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.StatusUpdate
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.StatusUpdateReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.StatusUpdateRepository
import java.time.ZonedDateTime
import java.util.UUID

@Service
class RejectOrderService(
  private val gateway: OrderByCaseIdGateway,
  private val repo: OrderRepository,
  private val statusUpdateRepo: StatusUpdateRepository,
) {
  @Transactional
  fun execute(caseId: String, dateTime: ZonedDateTime, reasons: List<RejectionReason>): UUID {
    val order = gateway.findOrderByCaseId(caseId)
      ?: throw EntityNotFoundException("Order with caseId $caseId does not exist")

    order.status = OrderStatus.REJECTED
    repo.save(order)

    val update = StatusUpdate(
      orderId = order.id,
      status = ProcessingStatus.REJECTED,
      datetimeOfStatusChange = dateTime,
      statusUpdatedBy = EMS_NAME,
    )
    update.statusUpdateReasons.addAll(
      reasons.map {
        StatusUpdateReason(statusUpdateId = update.id, section = it.section, details = it.details)
      },
    )
    statusUpdateRepo.save(update)

    return order.id
  }

  private companion object {
    const val EMS_NAME = "The Electronic Monitoring Service (EMS)"
  }
}
