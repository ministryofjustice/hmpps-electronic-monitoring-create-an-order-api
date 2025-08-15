package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SubmitOrderException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification.OrderListSpecification
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification.OrderSearchSpecification
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.*

@Service
class OrderService(
  val repo: OrderRepository,
  val fmsService: FmsService,
  @Value("\${settings.data-dictionary-version}") val defaultDataDictionaryVersion: String,
) {

  fun createOrder(username: String, createRecord: CreateOrderDto): Order {
    val order = Order()
    val dataDictionaryVersion = DataDictionaryVersion.entries.first { it.name == defaultDataDictionaryVersion }
    order.versions.add(
      OrderVersion(
        username = username,
        status = OrderStatus.IN_PROGRESS,
        type = createRecord.type,
        orderId = order.id,
        dataDictionaryVersion = dataDictionaryVersion,
      ),
    )

    order.versions[0].deviceWearer = DeviceWearer(
      versionId = order.versions[0].id,
    )

    order.versions[0].monitoringConditions = MonitoringConditions(
      versionId = order.versions[0].id,
    )

    repo.save(order)
    return order
  }

  fun deleteCurrentVersionForOrder(id: UUID, username: String) {
    val order = getOrder(id, username)

    order.deleteCurrentVersion()

    if (order.versions.isEmpty()) {
      repo.delete(order)
    } else {
      repo.save(order)
    }
  }

  fun getOrder(id: UUID, username: String): Order {
    val order = repo.findById(id).orElseThrow {
      EntityNotFoundException("Order with id $id does not exist")
    }

    if (order.status != OrderStatus.SUBMITTED && order.username != username) {
      throw EntityNotFoundException("Order ($id) for $username not found")
    }

    return order
  }

  fun createVariationFromExisting(originalOrderId: UUID, username: String): Order {
    val originalOrder = getOrder(originalOrderId, username)
    val originalVersion = originalOrder.getCurrentVersion()
    val newVersionId = originalOrder.getCurrentVersion().versionId + 1

    val newVariationOrder = Order()

    val newOrderVersion = OrderVersion(
      order = newVariationOrder,
      orderId = newVariationOrder.id,
      versionId = newVersionId,
      status = OrderStatus.IN_PROGRESS,
      type = RequestType.VARIATION,
      username = username,
      dataDictionaryVersion = originalVersion.dataDictionaryVersion,
      fmsResultId = null,
      fmsResultDate = null,
    )

    newOrderVersion.deviceWearer =
      originalVersion.deviceWearer?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.deviceWearerResponsibleAdult =
      originalVersion.deviceWearerResponsibleAdult?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.contactDetails =
      originalVersion.contactDetails?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.curfewConditions =
      originalVersion.curfewConditions?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.curfewReleaseDateConditions =
      originalVersion.curfewReleaseDateConditions?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.installationAndRisk =
      originalVersion.installationAndRisk?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.interestedParties =
      originalVersion.interestedParties?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.probationDeliveryUnit =
      originalVersion.probationDeliveryUnit?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.monitoringConditions =
      originalVersion.monitoringConditions?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.monitoringConditionsAlcohol =
      originalVersion.monitoringConditionsAlcohol?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.monitoringConditionsTrail =
      originalVersion.monitoringConditionsTrail?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.variationDetails =
      originalVersion.variationDetails?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.installationLocation =
      originalVersion.installationLocation?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
    newOrderVersion.installationAppointment =
      originalVersion.installationAppointment?.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)

    newOrderVersion.additionalDocuments =
      originalVersion.additionalDocuments.map {
        it.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
      }.toMutableList()
    newOrderVersion.addresses =
      originalVersion.addresses.map { it.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id) }.toMutableList()
    newOrderVersion.curfewTimeTable =
      originalVersion.curfewTimeTable.map {
        it.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
      }.toMutableList()
    newOrderVersion.enforcementZoneConditions =
      originalVersion.enforcementZoneConditions.map {
        it.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
      }.toMutableList()
    newOrderVersion.mandatoryAttendanceConditions =
      originalVersion.mandatoryAttendanceConditions.map {
        it.copy(id = UUID.randomUUID(), versionId = newOrderVersion.id)
      }.toMutableList()

    newVariationOrder.versions.add(newOrderVersion)

    repo.save(newVariationOrder)
    return newVariationOrder
  }

  fun submitOrder(id: UUID, username: String, fullName: String): Order {
    val order = getOrder(id, username)

    if (order.status == OrderStatus.SUBMITTED) {
      throw SubmitOrderException("This order has already been submitted")
    }

    if (order.status == OrderStatus.ERROR) {
      throw SubmitOrderException("This order has encountered an error and cannot be submitted")
    }

    if (order.status == OrderStatus.IN_PROGRESS && !order.isValid) {
      throw SubmitOrderException("Please complete all mandatory fields before submitting this form")
    }

    if (order.status == OrderStatus.IN_PROGRESS && order.isValid) {
      try {
        val submitResult = fmsService.submitOrder(order, FmsOrderSource.CEMO)
        order.fmsResultId = submitResult.id
        order.fmsResultDate = submitResult.submissionDate

        if (!submitResult.partialSuccess) {
          order.status = OrderStatus.ERROR
          repo.save(order)
          throw Exception(submitResult.error)
        } else if (!submitResult.attachmentSuccess) {
          order.status = OrderStatus.ERROR
          repo.save(order)
          throw SubmitOrderException("Error submit attachments to Serco")
        } else {
          order.status = OrderStatus.SUBMITTED
          order.getCurrentVersion().submittedBy = fullName
          repo.save(order)
        }
      } catch (e: Exception) {
        order.status = OrderStatus.ERROR
        repo.save(order)
        if (e is SubmitOrderException) {
          throw e
        }
        throw SubmitOrderException("The order could not be submitted to Serco", e)
      }
    }

    return order
  }

  fun listOrders(searchCriteria: OrderListCriteria): List<Order> = repo.findAll(
    OrderListSpecification(searchCriteria),
  )

  fun searchOrders(searchCriteria: OrderSearchCriteria): List<Order> = repo.findAll(
    OrderSearchSpecification(searchCriteria),
  )
}
