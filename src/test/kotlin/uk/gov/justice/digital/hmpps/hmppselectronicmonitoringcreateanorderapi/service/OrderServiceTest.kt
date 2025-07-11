package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification.OrderListSpecification
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification.OrderSearchSpecification
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
@JsonTest
class OrderServiceTest {
  private lateinit var repo: OrderRepository
  private lateinit var fmsService: FmsService
  private lateinit var service: OrderService

  @BeforeEach
  fun setup() {
    repo = mock(OrderRepository::class.java)
    fmsService = mock(FmsService::class.java)
    service = OrderService(repo, fmsService, "DDV4")
  }

  @Test
  fun `Create a new order for user and save to database`() {
    val result = service.createOrder("mockUser", CreateOrderDto())

    Assertions.assertThat(result.id).isNotNull()
    Assertions.assertThat(UUID.fromString(result.id.toString())).isEqualTo(result.id)
    Assertions.assertThat(result.username).isEqualTo("mockUser")
    Assertions.assertThat(result.status).isEqualTo(OrderStatus.IN_PROGRESS)
    Assertions.assertThat(result.dataDictionaryVersion).isEqualTo(DataDictionaryVersion.DDV4)
    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue).isEqualTo(result)
    }
  }

  @Test
  fun `Should create fms device wearer and monitoring order and save both id to database`() {
    val mockOrder = createReadyToSubmitOrder()
    reset(repo)

    val mockFmsResult = FmsSubmissionResult(
      orderId = mockOrder.getCurrentVersion().id,
      deviceWearerResult = FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        deviceWearerId = "mockDeviceWearerId",
      ),
      monitoringOrderResult = FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        monitoringOrderId = "mockMonitoringOrderId",
      ),
      orderSource = FmsOrderSource.CEMO,
      strategy = FmsSubmissionStrategyKind.ORDER,
    )
    whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(fmsService.submitOrder(any<Order>(), eq(FmsOrderSource.CEMO))).thenReturn(
      mockFmsResult,
    )
    service.submitOrder(mockOrder.id, "mockUser")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue.fmsResultId).isEqualTo(mockFmsResult.id)
    }
  }

  @Test
  fun `Should be able to list all orders`() {
    val mockOrder = createReadyToSubmitOrder()
    val mockCriteria = OrderListCriteria(username = "test")

    whenever(repo.findAll(ArgumentMatchers.any(OrderListSpecification::class.java))).thenReturn(listOf(mockOrder))

    val result = service.listOrders(mockCriteria)

    Assertions.assertThat(result).isEqualTo(listOf(mockOrder))
  }

  @Test
  fun `Should be able to search for orders`() {
    val mockOrder = createReadyToSubmitOrder()
    val mockCriteria = OrderSearchCriteria(searchTerm = "Bob Smith")

    whenever(repo.findAll(ArgumentMatchers.any(OrderSearchSpecification::class.java))).thenReturn(listOf(mockOrder))

    val result = service.searchOrders(mockCriteria)

    Assertions.assertThat(result).isEqualTo(listOf(mockOrder))
  }

  val mockStartDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusMonths(2)
  fun createReadyToSubmitOrder(noFixedAddress: Boolean = false): Order {
    val orderId = UUID.randomUUID()
    val versionId = UUID.randomUUID()
    val order = Order(
      id = orderId,
      versions = mutableListOf(
        OrderVersion(
          id = versionId,
          orderId = orderId,
          username = "mockUser",
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          dataDictionaryVersion = DataDictionaryVersion.DDV4,
        ),
      ),
    )
    order.deviceWearer = DeviceWearer(
      versionId = versionId,
      firstName = "John",
      lastName = "Smith",
      alias = "Johnny",
      dateOfBirth = ZonedDateTime.of(
        1990,
        1,
        1,
        1,
        1,
        1,
        1,
        ZoneId.systemDefault(),
      ),
      adultAtTimeOfInstallation = true,
      sex = "Male",
      gender = "Male",
      disabilities = "VISION,LEARNING_UNDERSTANDING_CONCENTRATING",
      interpreterRequired = true,
      language = "British Sign",
      pncId = "pncId",
      deliusId = "deliusId",
      nomisId = "nomisId",
      prisonNumber = "prisonNumber",
      homeOfficeReferenceNumber = "homeOfficeReferenceNumber",
      noFixedAbode = noFixedAddress,
    )

    order.deviceWearerResponsibleAdult = ResponsibleAdult(
      versionId = versionId,
      fullName = "Mark Smith",
      contactNumber = "07401111111",
    )

    val installationAddress = Address(
      versionId = versionId,
      addressLine1 = "24 Somewhere Street",
      addressLine2 = "Nowhere City",
      addressLine3 = "Random County",
      addressLine4 = "United Kingdom",
      postcode = "SW11 1NC",
      addressType = AddressType.INSTALLATION,
    )

    if (!noFixedAddress) {
      order.addresses.addAll(
        mutableListOf(
          Address(
            versionId = versionId,
            addressLine1 = "20 Somewhere Street",
            addressLine2 = "Nowhere City",
            addressLine3 = "Random County",
            addressLine4 = "United Kingdom",
            postcode = "SW11 1NC",
            addressType = AddressType.PRIMARY,
          ),
          Address(
            versionId = versionId,
            addressLine1 = "22 Somewhere Street",
            addressLine2 = "Nowhere City",
            addressLine3 = "Random County",
            addressLine4 = "United Kingdom",
            postcode = "SW11 1NC",
            addressType = AddressType.SECONDARY,
          ),
          installationAddress,
        ),
      )
    } else {
      order.addresses.addAll(
        mutableListOf(
          installationAddress,
        ),
      )
    }

    order.installationAndRisk = InstallationAndRisk(
      versionId = versionId,
      offence = "FRAUD_OFFENCES",
      riskDetails = "Danger",
      mappaLevel = "MAAPA 1",
      mappaCaseType = "CPPC (Critical Public Protection Case)",
    )

    order.contactDetails = ContactDetails(
      versionId = versionId,
      contactNumber = "07401111111",
    )
    order.monitoringConditions = MonitoringConditions(
      versionId = versionId,
      orderType = OrderType.COMMUNITY,
      orderTypeDescription = OrderTypeDescription.DAPOL,
      startDate = mockStartDate,
      endDate = mockEndDate,
      curfew = true,
      trail = true,
      exclusionZone = true,
      alcohol = true,
      caseId = "d8ea62e61bb8d610a10c20e0b24bcb85",
      conditionType = MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER,
    )
    val curfewConditions = CurfewConditions(
      versionId = versionId,
      startDate = mockStartDate,
      endDate = mockEndDate,
      curfewAddress = "PRIMARY,SECONDARY",
    )

    val curfewTimeTables = DayOfWeek.entries.map {
      CurfewTimeTable(
        versionId = versionId,
        dayOfWeek = it,
        startTime = "17:00",
        endTime = "09:00",
        curfewAddress = "PRIMARY_ADDRESS",
      )
    }
    order.curfewTimeTable.addAll(curfewTimeTables)
    val secondTimeTable = DayOfWeek.entries.map {
      CurfewTimeTable(
        versionId = versionId,
        dayOfWeek = it,
        startTime = "17:00",
        endTime = "09:00",
        curfewAddress = "SECONDARY_ADDRESS",
      )
    }
    order.curfewTimeTable.addAll(secondTimeTable)
    order.curfewConditions = curfewConditions

    order.curfewReleaseDateConditions = CurfewReleaseDateConditions(
      versionId = versionId,
      releaseDate = mockStartDate,
      startTime = "19:00",
      endTime = "23:00",
      curfewAddress = AddressType.PRIMARY,
    )

    order.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        versionId = versionId,
        description = "Mock Exclusion Zone",
        duration = "Mock Exclusion Duration",
        startDate = mockStartDate,
        endDate = mockEndDate,
        zoneType = EnforcementZoneType.EXCLUSION,
      ),
    )

    order.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        versionId = versionId,
        description = "Mock Inclusion Zone",
        duration = "Mock Inclusion Duration",
        startDate = mockStartDate,
        endDate = mockEndDate,
        zoneType = EnforcementZoneType.INCLUSION,
      ),
    )

    order.monitoringConditionsAlcohol = AlcoholMonitoringConditions(
      versionId = versionId,
      startDate = mockStartDate,
      endDate = mockEndDate,
      monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
      installationLocation = InstallationLocationType.PRIMARY,
    )

    order.monitoringConditionsTrail = TrailMonitoringConditions(
      versionId = versionId,
      startDate = mockStartDate,
      endDate = mockEndDate,
    )

    order.interestedParties = InterestedParties(
      versionId = versionId,
      responsibleOfficerName = "John Smith",
      responsibleOfficerPhoneNumber = "07401111111",
      responsibleOrganisation = "Avon and Somerset Constabulary",
      responsibleOrganisationRegion = "Mock Region",
      responsibleOrganisationEmail = "abc@def.com",
      notifyingOrganisation = "Mock Organisation",
      notifyingOrganisationName = "",
      notifyingOrganisationEmail = "",
    )

    return order
  }
}
