package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Service
abstract class OrderSectionServiceBase {
  @Autowired
  lateinit var orderRepo: OrderRepository

  internal fun findEditableOrder(id: UUID, username: String): Order {
    val order = orderRepo.findById(id).orElseThrow {
      EntityNotFoundException("An editable order with $id does not exist")
    }

    if (order.status !== OrderStatus.IN_PROGRESS) {
      throw EntityNotFoundException("An editable order with $id does not exist")
    }

    if (order.username != username) {
      throw EntityNotFoundException("An editable order with $id does not exist")
    }

    return order
  }

  internal fun getDefaultZonedDateTime(date: ZonedDateTime?, hours: Int, minutes: Int): ZonedDateTime? {
    if (date !== null) {
      return ZonedDateTime.of(
        date.year,
        date.monthValue,
        date.dayOfMonth,
        hours,
        minutes,
        0,
        0,
        ZoneId.of("Europe/London"),
      )
    }
    return null
  }
}
