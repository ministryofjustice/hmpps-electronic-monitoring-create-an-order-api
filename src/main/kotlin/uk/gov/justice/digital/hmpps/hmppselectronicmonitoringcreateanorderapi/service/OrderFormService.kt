package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import java.util.*

@Service
class OrderFormService(
  val repo: OrderFormRepository,
) {

  fun createOrderForm(username: String): OrderForm {
    val orderForm = OrderForm(username = username, status = FormStatus.IN_PROGRESS)
    orderForm.deviceWearer = DeviceWearer(orderId = orderForm.id)
    orderForm.monitoringConditions = MonitoringConditions(orderId = orderForm.id)
    repo.save(orderForm)
    return orderForm
  }
}
