package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.OffsetDateTime
import java.util.*

@Service
abstract class OrderSectionServiceBase {
  @Autowired
  lateinit var orderRepo: OrderRepository

  internal fun findEditableOrder(id: UUID, username: String): Order {
    val order = orderRepo.findById(id).orElseThrow {
      EntityNotFoundException(ValidationErrors.OrderSectionServiceBase.noEditableOrderExists(id))
    }

    if (order.status !== OrderStatus.IN_PROGRESS) {
      throw EntityNotFoundException(ValidationErrors.OrderSectionServiceBase.noEditableOrderExists(id))
    }

    if (order.username != username) {
      throw EntityNotFoundException(ValidationErrors.OrderSectionServiceBase.noEditableOrderExists(id))
    }

    return order
  }

  internal fun updateLastUpdatedByAndSaveOrder(order: Order): Order {
    val authentication = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    order.lastUpdatedBy = authentication.getUserFullName()
    order.lastUpdatedDateTime = OffsetDateTime.now()
    return orderRepo.save(order)
  }
}
