package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@Component
@Profile("local")
class LocalSearchDataSeeder(
  private val orderRepository: OrderRepository,
  private val fmsSubmissionResultRepository: FmsSubmissionResultRepository,
) : ApplicationRunner {

  @Transactional
  override fun run(args: ApplicationArguments) {
    val ordersToSeed = seedDefinitions.map(::createOrder)
    val newOrders = ordersToSeed.filterNot { orderRepository.existsById(it.id) }

    if (newOrders.isNotEmpty()) {
      orderRepository.saveAll(newOrders)
    }

    val existingFmsOrderIds = fmsSubmissionResultRepository.findAll().map { it.orderId }.toSet()
    val newFmsResults = seedDefinitions
      .filterNot { UUID.fromString(it.orderId) in existingFmsOrderIds }
      .mapNotNull { definition ->
        definition.caseId?.let { caseId ->
          FmsSubmissionResult(
            orderId = UUID.fromString(definition.orderId),
            strategy = FmsSubmissionStrategyKind.ORDER,
            orderSource = FmsOrderSource.CEMO,
            deviceWearerResult = FmsDeviceWearerSubmissionResult(
              status = SubmissionStatus.SUCCESS,
              deviceWearerId = caseId,
            ),
          )
        }
      }

    if (newFmsResults.isNotEmpty()) {
      fmsSubmissionResultRepository.saveAll(newFmsResults)
    }
  }

  private fun createOrder(definition: SeedDefinition): Order {
    val versionId = UUID.fromString(definition.versionId)
    val order = Order(id = UUID.fromString(definition.orderId))
    order.versions = mutableListOf(
      OrderVersion(
        id = versionId,
        orderId = order.id,
        username = "local-user",
        status = definition.status,
        dataDictionaryVersion = DataDictionaryVersion.DDV4,
        type = definition.type,
        fmsResultDate = definition.fmsResultDate,
        tags = definition.tags,
        ownerCohort = definition.ownerCohort,
        lastUpdatedBy = "Local User",
        lastUpdatedDateTime = OffsetDateTime.now(),
      ),
    )

    order.deviceWearer = DeviceWearer(
      versionId = versionId,
      firstName = definition.firstName,
      lastName = definition.lastName,
      dateOfBirth = ZonedDateTime.of(1990, 1, 15, 12, 0, 0, 0, ZoneId.of("UTC")),
      adultAtTimeOfInstallation = true,
      sex = "MALE",
      gender = "MALE",
      noFixedAbode = false,
      nomisId = definition.nomisId,
      pncId = definition.pncId,
      deliusId = definition.deliusId,
      prisonNumber = definition.prisonNumber,
      homeOfficeReferenceNumber = definition.homeOfficeReferenceNumber,
      complianceAndEnforcementPersonReference = definition.cepr,
      courtCaseReferenceNumber = definition.courtCaseReferenceNumber,
    )

    order.addresses += Address(
      versionId = versionId,
      addressType = AddressType.PRIMARY,
      addressLine1 = definition.addressLine1,
      addressLine2 = definition.addressLine2,
      addressLine3 = definition.addressLine3,
      postcode = definition.postcode,
    )

    order.monitoringConditions = MonitoringConditions(
      versionId = versionId,
      startDate = ZonedDateTime.now().plusDays(14),
      endDate = ZonedDateTime.now().plusMonths(6),
      orderType = OrderType.COMMUNITY,
    )

    if (definition.includeVersionHistory) {
      addVersionHistory(order)
    }

    return order
  }

  private fun addVersionHistory(order: Order) {
    val currentVersion = order.getCurrentVersion()
    val historyVersionId = UUID.fromString("00000000-0000-0000-0000-000000001201")
    order.versions += OrderVersion(
      id = historyVersionId,
      orderId = order.id,
      versionId = 1,
      username = currentVersion.username,
      status = OrderStatus.SUBMITTED,
      dataDictionaryVersion = currentVersion.dataDictionaryVersion,
      type = RequestType.VARIATION,
      fmsResultDate = OffsetDateTime.now().minusDays(1),
      deviceWearer = currentVersion.deviceWearer?.copy(
        id = UUID.fromString("00000000-0000-0000-0000-000000001202"),
        versionId = historyVersionId,
      ),
      addresses = currentVersion.addresses.map { address ->
        address.copy(
          id = UUID.randomUUID(),
          versionId = historyVersionId,
        )
      }.toMutableList(),
      monitoringConditions = currentVersion.monitoringConditions?.copy(
        id = UUID.fromString("00000000-0000-0000-0000-000000001203"),
        versionId = historyVersionId,
      ),
      tags = currentVersion.tags,
      lastUpdatedBy = currentVersion.lastUpdatedBy,
      lastUpdatedDateTime = OffsetDateTime.now(),
      ownerCohort = currentVersion.ownerCohort,
    )
  }

  private data class SeedDefinition(
    val orderId: String,
    val versionId: String,
    val firstName: String,
    val lastName: String,
    val status: OrderStatus,
    val type: RequestType,
    val tags: String,
    val ownerCohort: String,
    val addressLine1: String,
    val addressLine2: String,
    val addressLine3: String,
    val postcode: String,
    val nomisId: String? = null,
    val pncId: String? = null,
    val deliusId: String? = null,
    val prisonNumber: String? = null,
    val homeOfficeReferenceNumber: String? = null,
    val cepr: String? = null,
    val courtCaseReferenceNumber: String? = null,
    val caseId: String? = null,
    val includeVersionHistory: Boolean = false,
    val fmsResultDate: OffsetDateTime? = null,
  )

  companion object {
    private val seedDefinitions = listOf(
      SeedDefinition(
        orderId = "00000000-0000-0000-0000-000000000101",
        versionId = "00000000-0000-0000-0000-000000001101",
        firstName = "John",
        lastName = "Smith",
        status = OrderStatus.IN_PROGRESS,
        type = RequestType.REQUEST,
        tags = "LOCAL_SEARCH_SEED,PRISON,WAYLAND_PRISON",
        ownerCohort = "WAYLAND_PRISON",
        addressLine1 = "1 Search Street",
        addressLine2 = "Wayland",
        addressLine3 = "Norfolk",
        postcode = "NR14 8SE",
        nomisId = "A1234BZ",
        pncId = "PNC-LOCAL-001",
        prisonNumber = "A1234BZ",
        caseId = "CASE-LOCAL-001",
        includeVersionHistory = true,
      ),
      SeedDefinition(
        orderId = "00000000-0000-0000-0000-000000000102",
        versionId = "00000000-0000-0000-0000-000000001102",
        firstName = "Jane",
        lastName = "Doe",
        status = OrderStatus.SUBMITTED,
        type = RequestType.REQUEST,
        tags = "LOCAL_SEARCH_SEED,Probation",
        ownerCohort = "PROBATION",
        addressLine1 = "2 Search Road",
        addressLine2 = "Leeds",
        addressLine3 = "West Yorkshire",
        postcode = "LS1 1AA",
        deliusId = "D123456",
        cepr = "CEPR-LOCAL-002",
        caseId = "CASE-LOCAL-002",
        fmsResultDate = OffsetDateTime.now().minusDays(2),
      ),
      SeedDefinition(
        orderId = "00000000-0000-0000-0000-000000000103",
        versionId = "00000000-0000-0000-0000-000000001103",
        firstName = "Alex",
        lastName = "Taylor",
        status = OrderStatus.IN_PROGRESS,
        type = RequestType.VARIATION,
        tags = "LOCAL_SEARCH_SEED,Home office",
        ownerCohort = "HOME_OFFICE",
        addressLine1 = "3 Search Avenue",
        addressLine2 = "London",
        addressLine3 = "Greater London",
        postcode = "SW1A 1AA",
        homeOfficeReferenceNumber = "HO-LOCAL-003",
        courtCaseReferenceNumber = "CASE-LOCAL-003",
        caseId = "CASE-LOCAL-003",
      ),
    )
  }
}
