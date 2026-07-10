package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.ForbiddenException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SubmitOrderException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.TagFilter
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderInformationDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.VersionInformationDTO
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderListView
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification.OrderSearchSpecification
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.projections.OrderVersionListInformation
import java.time.ZonedDateTime
import java.util.*

@Service
@EnableConfigurationProperties(
  FeatureFlags::class,
)
class OrderService(
  val fmsService: FmsService,
  private val featureFlags: FeatureFlags,
  private val userCohortService: UserCohortService,
) : OrderSectionServiceBase() {

  fun createOrder(username: String, createRecord: CreateOrderDto): Order {
    val order = Order()
    val dataDictionaryVersion = featureFlags.dataDictionaryVersion
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

    updateLastUpdatedByAndSaveOrder(order)
    return order
  }

  fun deleteCurrentVersionForOrder(id: UUID, token: JwtAuthenticationToken) {
    val order = getOrder(id, token)

    order.deleteCurrentVersion()

    if (order.versions.isEmpty()) {
      orderRepo.delete(order)
    } else {
      orderRepo.save(order)
    }
  }

  fun getOrder(id: UUID, token: JwtAuthenticationToken): Order {
    val username = token.name

    val order = orderRepo.findById(id).orElseThrow {
      EntityNotFoundException("Order with id $id does not exist")
    }

    val userCohort = userCohortService.getUserCohort(token)
    val userPrisons = if (userCohort.cohort == Cohort.PRISON) {
      Prison.fromId(userCohort.activeCaseLoadId)
    } else {
      null
    }

    if (order.status != OrderStatus.SUBMITTED && order.username != username) {
      if (order.ownerCohort == null ||
        userPrisons.isNullOrEmpty() ||
        userPrisons.all { it.value != order.ownerCohort }
      ) {
        throw EntityNotFoundException("Order ($id) for $username not found")
      }
    }

    if (order.status == OrderStatus.SUBMITTED) {
      val filter = TagFilter.getTagFilterByUserCohort(userCohort)
      if (!filter.matchesTags(order.tags)) {
        throw ForbiddenException("Order forbidden", errorCode = 40301)
      }
    }

    return order
  }

  private fun isUserFromOriginalNotifyingOrganistion(
    token: JwtAuthenticationToken,
    notifyingOrganisation: String?,
  ): Boolean {
    val userCohort = userCohortService.getUserCohort(token)
    return userCohortService.matchesNotifyingOrg(userCohort.cohort, notifyingOrganisation)
  }

  fun createVersion(orderId: UUID, token: JwtAuthenticationToken, versionType: RequestType): Order {
    val order = getOrder(orderId, token)
    val currentVersion = order.getCurrentVersion()
    if (currentVersion.status != OrderStatus.SUBMITTED) {
      throw BadRequestException("Order latest version not submitted")
    }
    if (versionType == RequestType.AMEND_ORIGINAL_REQUEST) {
      currentVersion.type = RequestType.REJECTED
    }
    val newVersionNumber = currentVersion.versionId + 1
    val dataDictionaryVersion = featureFlags.dataDictionaryVersion
    val newOrderVersion = OrderVersion(
      orderId = orderId,
      versionId = newVersionNumber,
      status = OrderStatus.IN_PROGRESS,
      type = versionType,
      username = token.name,
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

        val currentIPs = currentVersion.interestedParties
        val isUserFromOriginalNotifyingOrganistion =
          isUserFromOriginalNotifyingOrganistion(token, currentIPs?.notifyingOrganisation)
        val isStartDateInFuture = order.getMonitoringStartDate()?.isAfter(ZonedDateTime.now()) == true

        interestedParties =
          currentVersion.interestedParties?.copy(
            versionId = this.id,
            id = UUID.randomUUID(),
            notifyingOrganisation = currentIPs?.notifyingOrganisation
              ?.takeIf { isUserFromOriginalNotifyingOrganistion },
            notifyingOrganisationName = currentIPs?.notifyingOrganisationName
              ?.takeIf { isUserFromOriginalNotifyingOrganistion },
            notifyingOrganisationEmail = currentIPs?.notifyingOrganisationEmail
              ?.takeIf { isUserFromOriginalNotifyingOrganistion },

            responsibleOrganisation = currentIPs?.responsibleOrganisation?.takeIf {
              isStartDateInFuture
            },
            responsibleOrganisationRegion = currentIPs?.responsibleOrganisationRegion?.takeIf {
              isStartDateInFuture
            },
            responsibleOrganisationEmail = currentIPs?.responsibleOrganisationEmail?.takeIf {
              isStartDateInFuture
            },
            responsibleOfficerName = currentIPs?.responsibleOfficerName?.takeIf {
              isStartDateInFuture
            },
            responsibleOfficerFirstName = currentIPs?.responsibleOfficerFirstName?.takeIf {
              isStartDateInFuture
            },
            responsibleOfficerLastName = currentIPs?.responsibleOfficerLastName?.takeIf {
              isStartDateInFuture
            },
            responsibleOfficerEmail = currentIPs?.responsibleOfficerEmail?.takeIf {
              isStartDateInFuture
            },
            responsibleOfficerPhoneNumber = currentIPs?.responsibleOfficerPhoneNumber?.takeIf {
              isStartDateInFuture
            },
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
        offenceAdditionalDetails =
          currentVersion.offenceAdditionalDetails?.copy(versionId = this.id, id = UUID.randomUUID())
        detailsOfInstallation =
          currentVersion.detailsOfInstallation?.copy(versionId = this.id, id = UUID.randomUUID())
        mappa =
          currentVersion.mappa?.copy(versionId = this.id, id = UUID.randomUUID())

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
        offences = currentVersion.offences.map {
          it.copy(versionId = this.id, id = UUID.randomUUID())
        }.toMutableList()
        dapoClauses = currentVersion.dapoClauses.map {
          it.copy(versionId = this.id, id = UUID.randomUUID())
        }.toMutableList()
      }

    order.versions.add(newOrderVersion)
    return updateLastUpdatedByAndSaveOrder(order)
  }

  fun submitOrder(id: UUID, token: JwtAuthenticationToken, fullName: String): Order {
    val order = getOrder(id, token)

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
          updateLastUpdatedByAndSaveOrder(order)
          throw Exception(submitResult.error)
        } else if (!submitResult.attachmentSuccess) {
          order.status = OrderStatus.ERROR
          updateLastUpdatedByAndSaveOrder(order)
          throw SubmitOrderException("Error submit attachments to Serco")
        } else {
          order.status = OrderStatus.SUBMITTED
          order.getCurrentVersion().submittedBy = fullName
          order.tags = getTags(order)
          updateLastUpdatedByAndSaveOrder(order)
        }
      } catch (e: Exception) {
        order.status = OrderStatus.ERROR
        updateLastUpdatedByAndSaveOrder(order)
        if (e is SubmitOrderException) {
          throw e
        }
        throw SubmitOrderException("The order could not be submitted to Serco", e)
      }
    }

    return order
  }

  fun getTags(order: Order): String {
    val notifyingOrganisation = order.interestedParties?.notifyingOrganisation!!

    return when (notifyingOrganisation) {
      NotifyingOrganisationDDv5.PRISON.name -> {
        var tags = "PRISON," + order.interestedParties?.notifyingOrganisationName!!

        if (order.deviceWearer?.adultAtTimeOfInstallation == false) {
          tags += ",Youth YOI"
        }
        tags
      }

      NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE.name -> {
        if (order.deviceWearer?.adultAtTimeOfInstallation == false) "Youth YCS" else "Adult YCS"
      }

      NotifyingOrganisationDDv5.PROBATION.name -> "Probation"
      NotifyingOrganisationDDv5.CIVIL_COUNTY_COURT.name -> "Civil Court"
      NotifyingOrganisationDDv5.FAMILY_COURT.name -> "Family Court"
      NotifyingOrganisationDDv5.HOME_OFFICE.name -> "Home Office"
      else -> ""
    }
  }

  fun listOrders(
    authentication: JwtAuthenticationToken,
    view: OrderListView = OrderListView.MY_ORDERS,
  ): List<OrderInformationDto> {
    val username = authentication.name
    val results = when (view) {
      OrderListView.MY_ORDERS -> orderRepo.findMyOrders(username)
      OrderListView.FAILED_ORDERS -> orderRepo.findFailedOrders(username)
      OrderListView.PRISON_ORDERS -> {
        val userCohort = userCohortService.getUserCohort(authentication)
        if (userCohort.cohort != Cohort.PRISON || userCohort.activeCaseLoadId == "CADM_I") {
          throw AccessDeniedException("Prison view is only available to prison users")
        }
        val caseLoadId = userCohort.activeCaseLoadId
          ?: throw AccessDeniedException("Prison user has no active caseload")
        val prisonNames = Prison.fromId(caseLoadId).map { it.value }
        if (prisonNames.isEmpty()) {
          emptyList()
        } else {
          orderRepo.findPrisonOrders(prisonNames)
        }
      }
    }

    return results.map { it.toListInformationDto() }
  }

  fun searchOrders(searchTerm: String, authentication: JwtAuthenticationToken): List<Order> {
    val userCohort = userCohortService.getUserCohort(authentication)

    val filter = TagFilter.getTagFilterByUserCohort(userCohort)
    val ownerCohort = getOwnerCohort(userCohort)
    val searchCriteria = OrderSearchCriteria(searchTerm, filter, ownerCohort)

    return orderRepo.findAll(
      OrderSearchSpecification(searchCriteria),
    )
  }

  private fun getOwnerCohort(cohort: UserCohort): String? = when (cohort.cohort) {
    Cohort.PRISON -> Prison.fromId(cohort.activeCaseLoadId).firstOrNull()?.name
    else -> cohort.cohort.name
  }

  fun getVersionInformation(orderId: UUID): List<VersionInformationDTO> {
    val order = orderRepo.findById(orderId).orElseThrow {
      EntityNotFoundException("Order with id $orderId does not exist")
    }

    return order.versions.map {
      it.toDTO()
    }.sortedByDescending { it.fmsResultDate }
  }

  private fun OrderVersionListInformation.toListInformationDto(): OrderInformationDto = OrderInformationDto(
    id = this.getId(),
    versionId = this.getVersionId(),
    status = this.getStatus(),
    type = this.getType(),
    firstName = this.getFirstName(),
    lastName = this.getLastName(),
    notifyingOrganisation = this.getNotifyingOrganisation(),
    lastUpdatedBy = this.getLastUpdatedBy(),
    lastUpdatedDateTime = this.getLastUpdatedDateTime(),
  )

  private fun OrderVersion.toDTO() = VersionInformationDTO(
    orderId = this.orderId,
    versionId = this.id,
    versionNumber = this.versionId,
    fmsResultDate = this.fmsResultDate,
    type = this.type,
    submittedBy = this.submittedBy,
    status = this.status,
    notifyingOrganisation = this.interestedParties?.notifyingOrganisation,
    notifyingOrganisationName = this.interestedParties?.notifyingOrganisationName,
  )

  fun getSpecificVersion(orderId: UUID, versionId: UUID): Order {
    val order = orderRepo.findById(orderId).orElseThrow {
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
