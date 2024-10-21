package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as fmsDeviceWearer

@ActiveProfiles("test")
@JsonTest
class OrderServiceTest {
  private lateinit var repo: OrderRepository
  private lateinit var fmsClient: FmsClient
  private lateinit var service: OrderService

  @BeforeEach
  fun setup() {
    repo = mock(OrderRepository::class.java)
    fmsClient = mock(FmsClient::class.java)
    service = OrderService(repo, fmsClient)
  }

  @Test
  fun `Create a new order for user and save to database`() {
    val result = service.createOrder("mockUser")

    Assertions.assertThat(result.id).isNotNull()
    Assertions.assertThat(UUID.fromString(result.id.toString())).isEqualTo(result.id)
    Assertions.assertThat(result.username).isEqualTo("mockUser")
    Assertions.assertThat(result.status).isEqualTo(OrderStatus.IN_PROGRESS)
    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue).isEqualTo(result)
    }
  }

  fun createReadyToSubmitOrder(): Order {
    val order = Order(
      username = "AUTH_ADM",
      status = OrderStatus.IN_PROGRESS,
    )
    order.deviceWearer = DeviceWearer(
      orderId = order.id,
      firstName = "John",
      lastName = "Smith",
      alias = "Johny",
      dateOfBirth = ZonedDateTime.of(1990, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
      adultAtTimeOfInstallation = true,
      sex = "Male",
      gender = "Male",
      disabilities = "Vision,Hearing",
    )

    order.deviceWearerResponsibleAdult = ResponsibleAdult(
      orderId = order.id,
      fullName = "Mark Smith",
      contactNumber = "07401111111",
    )
    order.addresses = mutableListOf(
      Address(
        orderId = order.id,

        addressLine1 = "20 Somewhere Street",
        addressLine2 = "Nowhere City",
        addressLine3 = "Random County",
        addressLine4 = "United Kingdom",
        postcode = "SW11 1NC",

        addressType = AddressType.PRIMARY,
      ),
      Address(
        orderId = order.id,

        addressLine1 = "22 Somewhere Street",
        addressLine2 = "Nowhere City",
        addressLine3 = "Random County",
        addressLine4 = "United Kingdom",
        postcode = "SW11 1NC",

        addressType = AddressType.PRIMARY,
      ),
    )
    order.installationAndRisk = InstallationAndRisk(
      orderId = order.id,
      riskOfSeriousHarm = "High",
      riskOfSelfHarm = "Low",
      riskDetails = "Danger",
      mappaLevel = "MAAPA 1",
      mappaCaseType = "CPPC (Critical Public Protection Case)",
    )

    order.deviceWearerContactDetails = DeviceWearerContactDetails(
      orderId = order.id,
      contactNumber = "07401111111",
    )
    order.monitoringConditions = MonitoringConditions(orderId = order.id)
    order.responsibleOfficer = ResponsibleOfficer(orderId = order.id)
    order.additionalDocuments = mutableListOf()
    val conditions = MonitoringConditions(
      orderId = order.id,
      orderType = "community",
      devicesRequired = arrayOf("Location - fitted,Alcohol (Remote Breath)"),
      startDate = ZonedDateTime.of(2100, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
      endDate = ZonedDateTime.of(2101, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
      curfew = true,
      trail = true,
      exclusionZone = true,
      alcohol = true,
      caseId = "d8ea62e61bb8d610a10c20e0b24bcb85",
      conditionType = "Requirement of Community Order",
    )
    val curfewConditions = CurfewConditions(
      orderId = order.id,
      startDate = ZonedDateTime.of(2100, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
      endDate = ZonedDateTime.of(2101, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
    )

    val curfewTimeTables = DayOfWeek.entries.map {
      CurfewTimeTable(
        orderId = order.id,
        dayOfWeek = it,
        startTime = "17:00",
        endTime = "09:00",
        curfewAddress = "PRIMARY_ADDRESS",
      )
    }
    order.curfewTimeTable.addAll(curfewTimeTables)
    val secondTimeTable = DayOfWeek.entries.map {
      CurfewTimeTable(
        orderId = order.id,
        dayOfWeek = it,
        startTime = "17:00",
        endTime = "09:00",
        curfewAddress = "SECONDARY_ADDRESS",
      )
    }
    order.curfewTimeTable.addAll(secondTimeTable)
    order.curfewConditions = curfewConditions

    val releaseDay = CurfewReleaseDateConditions(
      orderId = order.id,
      releaseDate = ZonedDateTime.of(2100, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
    )
    order.curfewReleaseDateConditions = releaseDay

    order.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        orderId = order.id,
        description = "Mock Exclusion Zone",
        duration = "Mock Exclusion Duration",
        startDate = ZonedDateTime.of(2100, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
        endDate = ZonedDateTime.of(2101, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
        zoneType = EnforcementZoneType.EXCLUSION,
      ),
    )

    val alcohol = AlcoholMonitoringConditions(
      orderId = order.id,
      monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
      installationLocation = AlcoholMonitoringInstallationLocationType.PRIMARY,
    )
    order.alcoholMonitoringConditions = alcohol

    val responsibleOfficer = ResponsibleOfficer(
      orderId = order.id,
      name = "John Smith",
      phoneNumber = "07401111111",
      organisation = "Avon and Somerset Constabulary",
      organisationRegion = "Mock Region",
      organisationPostCode = "AB11 1CD",
      organisationPhoneNumber = "07401111111",
      organisationEmail = "abc@def.com",
      notifyingOrganisation = "Mock Organisation",
    )

    order.responsibleOfficer = responsibleOfficer

    order.monitoringConditions = conditions
    return order
  }

  @Test
  fun `Should create fms device wearer and monitoring order and save both id to database`() {
    val mockOrder = createReadyToSubmitOrder()

    whenever(repo.findByUsernameAndId("mockUser", mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(fmsClient.createDeviceWearer(any<fmsDeviceWearer>(), eq(mockOrder.id))).thenReturn(
      FmsResponse(
        result = listOf(
          FmsResult("", "mockDeviceWearerId"),
        ),
      ),
    )

    whenever(fmsClient.createMonitoringOrder(any<MonitoringOrder>(), eq(mockOrder.id))).thenReturn(
      FmsResponse(
        result = listOf(
          FmsResult("", "mockMonitoringOrderId"),
        ),
      ),
    )

    service.submitOrder(mockOrder.id, "mockUser")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue.fmsDeviceWearerId).isEqualTo("mockDeviceWearerId")
      Assertions.assertThat(firstValue.fmsMonitoringOrderId).isEqualTo("mockMonitoringOrderId")
    }
  }
}
