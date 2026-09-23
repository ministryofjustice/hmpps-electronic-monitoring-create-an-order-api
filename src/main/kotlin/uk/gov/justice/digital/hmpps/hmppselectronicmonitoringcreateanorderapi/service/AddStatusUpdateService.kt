package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.StatusUpdate
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.StatusUpdateReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class StatusUpdateReasonRequest(val section: String, val details: String)

@Service
class AddStatusUpdateService(private val gateway: OrderByCaseIdGateway, private val repo: OrderRepository) {
  fun execute(caseId: String, status: ProcessingStatus, dateTime: String, reasons: List<StatusUpdateReasonRequest>) {
    val order = gateway.findOrderByCaseId(caseId)
      ?: throw EntityNotFoundException("Order with caseId $caseId does not exist")

    val dateTime = ZonedDateTime.ofInstant(Instant.parse(dateTime), ZoneId.of("Europe/London"))

    val statusUpdate = StatusUpdate(
      versionId = order.versionId,
      status = status,
      datetimeOfStatusChange = dateTime,
    )
    val statusUpdateReasons =
      reasons.map {
        StatusUpdateReason(
          statusUpdateId = statusUpdate.id,
          section = it.section,
          details = it.details,
        )
      }

    statusUpdate.statusUpdateReasons.addAll(statusUpdateReasons)

    order.statusUpdates.add(statusUpdate)

    repo.save(order)
  }
}
