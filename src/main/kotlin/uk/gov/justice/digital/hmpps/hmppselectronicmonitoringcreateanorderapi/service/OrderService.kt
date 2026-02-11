package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SubmitOrderException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.VersionInformationDTO
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
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

  fun createVersion(orderId: UUID, username: String, versionType: RequestType): Order {
    val order = getOrder(orderId, username)
    val currentVersion = order.getCurrentVersion()
    if (currentVersion.status != OrderStatus.SUBMITTED) {
      throw BadRequestException("Order latest version not submitted")
    }
    if (versionType == RequestType.AMEND_ORIGINAL_REQUEST) {
      currentVersion.type = RequestType.REJECTED
    }
    val newVersionNumber = currentVersion.versionId + 1
    val dataDictionaryVersion = DataDictionaryVersion.entries.first { it.name == defaultDataDictionaryVersion }
    val newOrderVersion = OrderVersion(
      orderId = orderId,
      versionId = newVersionNumber,
      status = OrderStatus.IN_PROGRESS,
      type = versionType,
      username = username,
      dataDictionaryVersion = dataDictionaryVersion,
      fmsResultId = null,
      fmsResultDate = null,
    )
      .apply {
        variationDetails = null

        orderParameters =
          currentVersion.orderParameters?.copy(versionId = this.id, id = UUID.randomUUID())
        deviceWearer =
          currentVersion.deviceWearer?.copy(versionId = this.id, id = UUID.randomUUID())
        deviceWearerResponsibleAdult =
          currentVersion.deviceWearerResponsibleAdult?.copy(versionId = this.id, id = UUID.randomUUID())
        contactDetails =
          currentVersion.contactDetails?.copy(versionId = this.id, id = UUID.randomUUID())
        curfewConditions =
          currentVersion.curfewConditions?.copy(versionId = this.id, id = UUID.randomUUID())
        curfewReleaseDateConditions =
          currentVersion.curfewReleaseDateConditions?.copy(versionId = this.id, id = UUID.randomUUID())
        installationAndRisk =
          currentVersion.installationAndRisk?.copy(versionId = this.id, id = UUID.randomUUID())
        interestedParties =
          currentVersion.interestedParties?.copy(
            versionId = this.id,
            id = UUID.randomUUID(),
            notifyingOrganisation = null,
            notifyingOrganisationName = null,
            notifyingOrganisationEmail = null,
          )
        probationDeliveryUnit =
          currentVersion.probationDeliveryUnit?.copy(versionId = this.id, id = UUID.randomUUID())
        monitoringConditions =
          currentVersion.monitoringConditions?.copy(
            versionId = this.id,
            id = UUID.randomUUID(),
            startDate = null,
            endDate = null,
          )
        monitoringConditionsAlcohol =
          currentVersion.monitoringConditionsAlcohol?.copy(versionId = this.id, id = UUID.randomUUID())
        monitoringConditionsTrail =
          currentVersion.monitoringConditionsTrail?.copy(versionId = this.id, id = UUID.randomUUID())
        installationLocation =
          currentVersion.installationLocation?.copy(versionId = this.id, id = UUID.randomUUID())
        installationAppointment =
          currentVersion.installationAppointment?.copy(versionId = this.id, id = UUID.randomUUID())

        additionalDocuments =
          currentVersion.additionalDocuments.map {
            it.copy(versionId = this.id, id = UUID.randomUUID())
          }.toMutableList()
        addresses =
          currentVersion.addresses.map {
            it.copy(versionId = this.id, id = UUID.randomUUID())
          }.toMutableList()
        curfewTimeTable =
          currentVersion.curfewTimeTable.map {
            it.copy(versionId = this.id, id = UUID.randomUUID())
          }.toMutableList()
        enforcementZoneConditions =
          currentVersion.enforcementZoneConditions.map {
            it.copy(versionId = this.id, id = UUID.randomUUID())
          }.toMutableList()
        mandatoryAttendanceConditions =
          currentVersion.mandatoryAttendanceConditions.map {
            it.copy(versionId = this.id, id = UUID.randomUUID())
          }.toMutableList()
      }

    order.versions.add(newOrderVersion)
    return repo.save(order)
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
          order.tags = getTags(order)
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

  fun getTags(order: Order): String {
    var tags = ""
    if (order.interestedParties?.notifyingOrganisation!! == NotifyingOrganisation.PRISON.name) {
      tags = "PRISON,${order.interestedParties?.notifyingOrganisationName!!}"
    }
    if (order.interestedParties?.notifyingOrganisation!! == NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE.name) {
      tags = "YOUTH_YCS"
    }
    return tags
  }

  fun listOrders(searchCriteria: OrderListCriteria): List<Order> = repo.findAll(
    OrderListSpecification(searchCriteria),
  )

  fun searchOrders(searchCriteria: OrderSearchCriteria): List<Order> = repo.findAll(
    OrderSearchSpecification(searchCriteria),
  )

  fun getVersionInformation(orderId: UUID): List<VersionInformationDTO> {
    val order = repo.findById(orderId).orElseThrow {
      EntityNotFoundException("Order with id $orderId does not exist")
    }

    return order.versions.map {
      it.toDTO()
    }.sortedByDescending { it.fmsResultDate }
  }

  private fun OrderVersion.toDTO() = VersionInformationDTO(
    orderId = this.orderId,
    versionId = this.id,
    versionNumber = this.versionId,
    fmsResultDate = this.fmsResultDate,
    type = this.type,
    submittedBy = this.submittedBy,
    status = this.status,
  )

  fun getSpecificVersion(orderId: UUID, versionId: UUID): Order {
    val order = repo.findById(orderId).orElseThrow {
      EntityNotFoundException("Order with id $orderId does not exist")
    }
    val specificVersion = order.getSpecificVersion(versionId)
      ?: throw EntityNotFoundException("Version does not exist for orderId $orderId and versionId $versionId")

    return order.copy(versions = mutableListOf(specificVersion))
  }

  fun getFmsDeviceWearerPayload(orderId: UUID, versionId: UUID): String {
    val version = getSpecificVersion(orderId, versionId)
    if (version.status === OrderStatus.IN_PROGRESS) {
      throw BadRequestException("This order is not submitted")
    }
    return fmsService.getFmsDeviceWearerSubmissionResultById(version.fmsResultId!!)
  }

  fun getFmsMonitoringOrderPayload(orderId: UUID, versionId: UUID): String {
    val version = getSpecificVersion(orderId, versionId)
    if (version.status === OrderStatus.IN_PROGRESS) {
      throw BadRequestException("This order is not submitted")
    }
    return fmsService.getFmsMonitoringOrderSubmissionResultByOrderId(version.fmsResultId!!)
  }
}
