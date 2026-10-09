package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.ManageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.ForbiddenException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.OrderChangeException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SubmitOrderException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.TagFilter
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderInformationDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderInformationMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderInformationPageDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderSearchResultDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.VersionInformationDTO
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderListView
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
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
  private val manageUserApi: ManageUserApi,
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

    if (order.status != OrderStatus.SUBMITTED && order.username != username) {
      if (userCohort.cohort == Cohort.PRISON) {
        val userPrisons = Prison.fromId(userCohort.activeCaseLoadId)

        if (order.ownerCohort == null ||
          userPrisons.all { it.name != order.ownerCohort }
        ) {
          // allow admin user to all draft orders
          if (userCohort.activeCaseLoadId != "CADM_I") {
            throw ForbiddenException("Order forbidden", errorCode = 40301)
          }
        }
      } else {
        if (order.ownerCohort != userCohort.cohort.name) {
          throw ForbiddenException("Order forbidden", errorCode = 40301)
        }
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

  private fun isUserFromOriginalNotifyingOrganisation(
    token: JwtAuthenticationToken,
    notifyingOrganisation: String?,
  ): Boolean {
    val userCohort = userCohortService.getUserCohort(token)
    return userCohortService.matchesNotifyingOrg(userCohort.cohort, notifyingOrganisation)
  }

  private fun <T> List<T>.cloneItems(transform: (T) -> T): MutableList<T> = map(transform).toMutableList()

  private val allowNewVersionStatuses = setOf(OrderStatus.SUBMITTED, OrderStatus.REJECTED)

  fun getCaseState(order: Order): CaseState = fmsService.getCaseState(order)

  fun canCreateNewVersion(order: Order, caseState: CaseState): Boolean = when (order.status) {
    OrderStatus.SUBMITTED ->
      caseState in
        setOf(CaseState.CLOSED, CaseState.RESOLVED, CaseState.CANCELLED, CaseState.UNKNOWN)
    OrderStatus.REJECTED -> caseState in setOf(CaseState.CLOSED, CaseState.CANCELLED)
    else -> false
  }

  private fun requireCaseState(
    order: Order,
    caseState: CaseState,
    allowedStates: Set<CaseState>,
    allowUnknownForVariation: Boolean = false,
  ) {
    val unknownAllowed =
      allowUnknownForVariation && order.status in allowNewVersionStatuses && caseState == CaseState.UNKNOWN
    if (!canCreateNewVersion(order, caseState) && !unknownAllowed) {
      throw OrderChangeException(
        when {
          caseState == CaseState.UNKNOWN -> "ORDER_CASE_STATE_UNAVAILABLE"
          caseState in setOf(
            CaseState.NEW,
            CaseState.OPEN,
            CaseState.AWAITING_INFO,
            CaseState.AWAITING_VALIDATION,
            CaseState.AWAITING_APPROVAL,
          ) ->
            "ORDER_CASE_STILL_PROCESSING"
          else -> "ORDER_VERSION_NOT_AVAILABLE"
        },
        "A new version cannot be created for the order's current state",
      )
    }
    if (caseState !in allowedStates && !unknownAllowed) {
      throw OrderChangeException(
        if (caseState == CaseState.CANCELLED) "ORDER_CASE_REJECTED" else "ORDER_CASE_NOT_REJECTED",
        "This operation is not available for the order's FMS case state",
      )
    }
  }

  fun createVersion(orderId: UUID, token: JwtAuthenticationToken, versionType: RequestType): Order {
    val order = getOrder(orderId, token)
    val currentVersion = order.getCurrentVersion()
    if (currentVersion.status !in allowNewVersionStatuses && currentVersion.status != OrderStatus.IN_PROGRESS) {
      throw BadRequestException("New order version is not allowed for order with status ${currentVersion.status}")
    }
    if (currentVersion.status != OrderStatus.IN_PROGRESS) {
      val caseState = fmsService.getCaseState(order)
      requireCaseState(
        order,
        caseState,
        setOf(CaseState.CLOSED, CaseState.RESOLVED),
        allowUnknownForVariation = versionType in RequestType.VARIATION_TYPES,
      )
    }
    val sourceVersion = when {
      currentVersion.status == OrderStatus.IN_PROGRESS || versionType == RequestType.AMEND_ORIGINAL_REQUEST ->
        currentVersion
      else -> fmsService.getLatestOrderVersion(order) ?: currentVersion
    }
    val newOrderVersion = buildCopiedVersion(
      sourceOrder = order,
      sourceVersion = sourceVersion,
      targetOrderId = order.id,
      versionNumber = currentVersion.versionId + 1,
      versionType = versionType,
      token = token,
    )
    order.versions.add(newOrderVersion)
    order.recalculateMonitoringStartEndDate()
    return updateLastUpdatedByAndSaveOrder(order)
  }

  fun createNewOrderFromRejected(orderId: UUID, token: JwtAuthenticationToken): Order {
    val sourceOrder = getOrder(orderId, token)
    val sourceVersion = sourceOrder.getCurrentVersion()
    if (sourceVersion.status != OrderStatus.SUBMITTED && sourceVersion.status != OrderStatus.REJECTED) {
      throw OrderChangeException(
        "ORDER_VERSION_NOT_AVAILABLE",
        "The latest order version is not eligible for replacement",
      )
    }

    val caseState = fmsService.getCaseState(sourceOrder)
    requireCaseState(sourceOrder, caseState, setOf(CaseState.CANCELLED))

    val sourceType = if (sourceVersion.type == RequestType.REJECTED) {
      sourceOrder.versions
        .filter { it.versionId < sourceVersion.versionId && it.type != RequestType.REJECTED }
        .maxByOrNull { it.versionId }
        ?.type
        ?: throw OrderChangeException(
          "ORDER_SOURCE_TYPE_UNAVAILABLE",
          "The rejected order's original type cannot be determined",
        )
    } else {
      sourceVersion.type
    }
    val replacementType = if (sourceType in RequestType.VARIATION_TYPES) RequestType.VARIATION else RequestType.REQUEST
    val replacementVersion = buildCopiedVersion(
      sourceOrder = sourceOrder,
      sourceVersion = sourceVersion,
      targetOrderId = sourceOrder.id,
      versionNumber = sourceVersion.versionId + 1,
      versionType = replacementType,
      token = token,
    )
    sourceOrder.versions.add(replacementVersion)
    sourceOrder.recalculateMonitoringStartEndDate()
    return updateLastUpdatedByAndSaveOrder(sourceOrder)
  }

  private fun buildCopiedVersion(
    sourceOrder: Order,
    sourceVersion: OrderVersion,
    targetOrderId: UUID,
    versionNumber: Int,
    versionType: RequestType,
    token: JwtAuthenticationToken,
  ): OrderVersion = OrderVersion(
    orderId = targetOrderId,
    versionId = versionNumber,
    status = OrderStatus.IN_PROGRESS,
    type = versionType,
    username = token.name,
    dataDictionaryVersion = featureFlags.dataDictionaryVersion,
  ).apply {
    val newVersionId = id
    variationDetails = null
    isSentencingAct = sourceOrder.isSentencingAct

    orderParameters = sourceVersion.orderParameters?.copy(versionId = newVersionId, id = UUID.randomUUID())
    deviceWearer = sourceVersion.deviceWearer?.copy(versionId = newVersionId, id = UUID.randomUUID())
    deviceWearerResponsibleAdult =
      sourceVersion.deviceWearerResponsibleAdult?.copy(versionId = newVersionId, id = UUID.randomUUID())
    contactDetails = sourceVersion.contactDetails?.copy(versionId = newVersionId, id = UUID.randomUUID())
    curfewConditions = sourceVersion.curfewConditions?.copy(versionId = newVersionId, id = UUID.randomUUID())
    curfewReleaseDateConditions =
      sourceVersion.curfewReleaseDateConditions?.copy(versionId = newVersionId, id = UUID.randomUUID())

    val currentIPs = sourceVersion.interestedParties
    val sameNotifyingOrganisation =
      isUserFromOriginalNotifyingOrganisation(token, currentIPs?.notifyingOrganisation)
    val startDateIsInFuture = sourceOrder.getMonitoringStartDate()?.isAfter(ZonedDateTime.now()) == true
    interestedParties = currentIPs?.copy(
      versionId = newVersionId,
      id = UUID.randomUUID(),
      notifyingOrganisation = currentIPs.notifyingOrganisation?.takeIf { sameNotifyingOrganisation },
      notifyingOrganisationName = currentIPs.notifyingOrganisationName?.takeIf { sameNotifyingOrganisation },
      notifyingOrganisationEmail = currentIPs.notifyingOrganisationEmail?.takeIf { sameNotifyingOrganisation },
      responsibleOrganisation = currentIPs.responsibleOrganisation?.takeIf { startDateIsInFuture },
      responsibleOrganisationRegion = currentIPs.responsibleOrganisationRegion?.takeIf { startDateIsInFuture },
      responsibleOrganisationEmail = currentIPs.responsibleOrganisationEmail?.takeIf { startDateIsInFuture },
      responsibleOfficerName = currentIPs.responsibleOfficerName?.takeIf { startDateIsInFuture },
      responsibleOfficerFirstName = currentIPs.responsibleOfficerFirstName?.takeIf { startDateIsInFuture },
      responsibleOfficerLastName = currentIPs.responsibleOfficerLastName?.takeIf { startDateIsInFuture },
      responsibleOfficerEmail = currentIPs.responsibleOfficerEmail?.takeIf { startDateIsInFuture },
      responsibleOfficerPhoneNumber = currentIPs.responsibleOfficerPhoneNumber?.takeIf { startDateIsInFuture },
    )
    probationDeliveryUnit = sourceVersion.probationDeliveryUnit?.copy(versionId = newVersionId, id = UUID.randomUUID())
    monitoringConditions = sourceVersion.monitoringConditions?.copy(
      versionId = newVersionId,
      id = UUID.randomUUID(),
      startDate = null,
      endDate = null,
    )
    monitoringConditionsAlcohol =
      sourceVersion.monitoringConditionsAlcohol?.copy(versionId = newVersionId, id = UUID.randomUUID())
    monitoringConditionsTrail =
      sourceVersion.monitoringConditionsTrail?.copy(versionId = newVersionId, id = UUID.randomUUID())
    installationLocation = sourceVersion.installationLocation?.copy(versionId = newVersionId, id = UUID.randomUUID())
    installationAppointment =
      sourceVersion.installationAppointment?.copy(versionId = newVersionId, id = UUID.randomUUID())
    offenceAdditionalDetails =
      sourceVersion.offenceAdditionalDetails?.copy(versionId = newVersionId, id = UUID.randomUUID())
    detailsOfInstallation =
      sourceVersion.detailsOfInstallation?.copy(versionId = newVersionId, id = UUID.randomUUID())
    mappa = sourceVersion.mappa?.copy(versionId = newVersionId, id = UUID.randomUUID())
    additionalDocuments = sourceVersion.additionalDocuments.cloneItems {
      it.copy(versionId = newVersionId, id = UUID.randomUUID())
    }
    addresses = sourceVersion.addresses.cloneItems { it.copy(versionId = newVersionId, id = UUID.randomUUID()) }
    curfewTimeTable = sourceVersion.curfewTimeTable.cloneItems {
      it.copy(versionId = newVersionId, id = UUID.randomUUID())
    }
    enforcementZoneConditions = sourceVersion.enforcementZoneConditions.cloneItems {
      it.copy(versionId = newVersionId, id = UUID.randomUUID())
    }
    mandatoryAttendanceConditions = sourceVersion.mandatoryAttendanceConditions.cloneItems {
      it.copy(versionId = newVersionId, id = UUID.randomUUID())
    }
    offences = sourceVersion.offences.cloneItems { it.copy(versionId = newVersionId, id = UUID.randomUUID()) }
    dapoClauses = sourceVersion.dapoClauses.cloneItems { it.copy(versionId = newVersionId, id = UUID.randomUUID()) }
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
          order.submittedBy = fullName
          order.submittedByEmail = runCatching { manageUserApi.getUserEmail(token.token) }.getOrNull()
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
    page: Int = 0,
    size: Int = DEFAULT_ORDER_LIST_PAGE_SIZE,
  ): OrderInformationPageDto {
    if (page < 0) throw BadRequestException("Page must be zero or greater")
    if (size !in 1..MAX_ORDER_LIST_PAGE_SIZE) {
      throw BadRequestException("Page size must be between 1 and $MAX_ORDER_LIST_PAGE_SIZE")
    }
    val pageable = PageRequest.of(page, size)
    val username = authentication.name
    val results: Slice<OrderVersionListInformation> = when (view) {
      OrderListView.MY_ORDERS -> orderRepo.findMyOrders(username, pageable)
      OrderListView.FAILED_ORDERS -> orderRepo.findFailedOrders(username, pageable)
      OrderListView.PRISON_ORDERS -> {
        val userCohort = userCohortService.getUserCohort(authentication)
        if (userCohort.cohort != Cohort.PRISON || userCohort.activeCaseLoadId == "CADM_I") {
          throw AccessDeniedException("Prison view is only available to prison users")
        }
        val caseLoadId = userCohort.activeCaseLoadId
          ?: throw AccessDeniedException("Prison user has no active caseload")
        val prisonNames = Prison.fromId(caseLoadId).map { it.name }
        if (prisonNames.isEmpty()) {
          SliceImpl(emptyList(), pageable, false)
        } else {
          orderRepo.findPrisonOrders(prisonNames, pageable)
        }
      }
      OrderListView.HOME_OFFICE_ORDERS -> {
        val userCohort = userCohortService.getUserCohort(authentication)
        if (userCohort.cohort != Cohort.HOME_OFFICE) {
          throw AccessDeniedException("Home Office view is only available to Home Office users")
        }
        orderRepo.findHomeOfficeOrders(pageable)
      }
    }

    return results.toOrderInformationPageDto()
  }

  fun searchOrders(searchTerm: String, authentication: JwtAuthenticationToken): List<OrderSearchResultDto> {
    val userCohort = userCohortService.getUserCohort(authentication)

    val filter = TagFilter.getTagFilterByUserCohort(userCohort)
    val ownerCohort = getOwnerCohort(userCohort)
    val searchCriteria = OrderSearchCriteria(searchTerm, filter, ownerCohort)

    return orderRepo.searchOrders(searchCriteria)
  }

  fun updateOrderOwner(orderId: UUID, token: JwtAuthenticationToken, newOwner: String): Order {
    val order = getOrder(orderId, token)
    order.username = newOwner
    updateLastUpdatedByAndSaveOrder(order)
    return order
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
    monitoringConditions = this.getStartDate()?.let { OrderInformationMonitoringConditionsDto(startDate = it) },
    lastUpdatedBy = this.getLastUpdatedBy(),
    lastUpdatedDateTime = this.getLastUpdatedDateTime(),
  )

  private fun Slice<OrderVersionListInformation>.toOrderInformationPageDto() = OrderInformationPageDto(
    content = content.map { it.toListInformationDto() },
    page = number,
    size = size,
    hasNext = hasNext(),
  )

  companion object {
    const val DEFAULT_ORDER_LIST_PAGE_SIZE = 50
    const val MAX_ORDER_LIST_PAGE_SIZE = 100
  }

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
    lastUpdatedDateTime = this.lastUpdatedDateTime,
    lastUpdatedBy = this.lastUpdatedBy,
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

  fun updateIsSentencingAct(orderId: UUID, isSentencingAct: Boolean, authentication: JwtAuthenticationToken) {
    val order = getOrder(orderId, authentication)
    order.isSentencingAct = isSentencingAct
    updateLastUpdatedByAndSaveOrder(order)
  }
}
