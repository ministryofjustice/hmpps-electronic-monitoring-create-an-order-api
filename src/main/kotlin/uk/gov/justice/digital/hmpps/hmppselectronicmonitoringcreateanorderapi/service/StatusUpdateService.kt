package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.StatusUpdateDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.StatusUpdateRepository
import java.util.UUID

@Service
class StatusUpdateService(
  private val orderRepo: OrderRepository,
  private val statusUpdateRepo: StatusUpdateRepository,
) {
  @Transactional(readOnly = true)
  fun getStatusUpdates(orderId: UUID): List<StatusUpdateDto> {
    if (!orderRepo.existsById(orderId)) {
      throw EntityNotFoundException("Order with id $orderId does not exist")
    }

    return statusUpdateRepo.findAllByOrderId(orderId)
      .sortedByDescending { it.datetimeOfStatusChange }
      .map { StatusUpdateDto.from(it) }
  }
}
