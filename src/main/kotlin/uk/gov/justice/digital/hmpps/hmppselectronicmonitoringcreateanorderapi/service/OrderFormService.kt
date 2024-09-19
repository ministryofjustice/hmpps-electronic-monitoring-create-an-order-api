package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.gateway.SercoGateway
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository

@Service
class OrderFormService(
  val repo: OrderFormRepository,
  val sercoGateway: SercoGateway
) {

  fun createOrderForm(title: String, username: String): OrderForm {
    val orderForm = OrderForm(title = title, username = username, status = FormStatus.IN_PROGRESS)
    repo.save(orderForm)
    return orderForm
  }

  fun submitOrderForm(): SubmissionResult {
    var result = SubmissionResult(false)
    val deviceWear = sercoGateway.createDeviceWeaer()
    return result
  }
}
