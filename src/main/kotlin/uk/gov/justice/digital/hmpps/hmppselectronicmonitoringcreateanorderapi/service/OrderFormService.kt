package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.SercoClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as FmsDeviceWearer

@Service
class OrderFormService(
  val repo: OrderFormRepository,
  val sercoClient: SercoClient,
) {

  fun createOrderForm(username: String): OrderForm {
    val orderForm = OrderForm(
      username = username,
      status = FormStatus.IN_PROGRESS,
    )
    orderForm.deviceWearer = DeviceWearer(orderId = orderForm.id)
    orderForm.deviceWearerContactDetails = DeviceWearerContactDetails(orderId = orderForm.id)
    orderForm.monitoringConditions = MonitoringConditions(orderId = orderForm.id)
    orderForm.responsibleOfficer = ResponsibleOfficer(orderId = orderForm.id)
    orderForm.installationAndRisk = InstallationAndRisk(orderId = orderForm.id)
    orderForm.additionalDocuments = mutableListOf()
    repo.save(orderForm)
    return orderForm
  }

  fun submitOrderForm(id: UUID, username: String) {
    val order = getOrderForm(username, id)!!

    val fmsDeviceWearer = FmsDeviceWearer.fromCemoOrder(order)
    val createDeviceWearerResult = sercoClient.createDeviceWeaer(fmsDeviceWearer, orderId = id)
    // TODO: Create Serco Order
    // TODO: Upload attachments
    order.fmsDeviceWearerId = createDeviceWearerResult.result.first().id

    repo.save(order)
  }

  fun listOrderFormsForUser(username: String): List<OrderForm> {
    return repo.findByUsername(
      username,
    )
  }

  fun getOrderForm(username: String, id: UUID): OrderForm? {
    return repo.findByUsernameAndId(
      username,
      id,
    ).orElseThrow {
      EntityNotFoundException("Order ($id) for $username not found")
    }
  }
}
