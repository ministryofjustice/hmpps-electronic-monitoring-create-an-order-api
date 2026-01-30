package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.adapter.out.persistence

import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.domain.model.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.ports.out.GetOrderPort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.ports.out.UpdateOrderPort
import java.util.*

@Service
class OrderPersistenceAdapter :
  GetOrderPort,
  UpdateOrderPort {
  @Autowired
  lateinit var orderRepo: OrderRepository

  @Autowired
  lateinit var orderMapper: OrderMapper

  override fun getOrderById(orderId: UUID, username: String): Order {
    val orderEntity = orderRepo.findById(orderId).orElseThrow {
      EntityNotFoundException(ValidationErrors.OrderSectionServiceBase.noEditableOrderExists(orderId))
    }

    if (orderEntity.status !== OrderStatus.IN_PROGRESS) {
      throw EntityNotFoundException(ValidationErrors.OrderSectionServiceBase.noEditableOrderExists(orderId))
    }

    if (orderEntity.username != username) {
      throw EntityNotFoundException(ValidationErrors.OrderSectionServiceBase.noEditableOrderExists(orderId))
    }

    return orderMapper.mapToDomainEntity(orderEntity)
  }

  override fun updateOrder(order: Order) {
    val orderEntity = orderMapper.mapToJpaEntity(order)
    orderRepo.save(orderEntity)
  }
}
