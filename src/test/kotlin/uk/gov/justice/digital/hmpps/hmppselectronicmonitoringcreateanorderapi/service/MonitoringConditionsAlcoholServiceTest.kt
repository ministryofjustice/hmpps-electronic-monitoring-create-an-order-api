package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.MonitoringConditionsAlcoholRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateAlcoholMonitoringConditionsDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
@JsonTest
class MonitoringConditionsAlcoholServiceTest {
  private lateinit var alcoholMonitoringConditionsService: MonitoringConditionsAlcoholService
  private lateinit var alcoholMonitoringConditionsRepo: MonitoringConditionsAlcoholRepository
  private lateinit var addressRepo: AddressRepository
  private lateinit var orderRepo: OrderRepository

  private val mockOrderId: UUID = UUID.fromString("da69b6d1-fb7f-4513-aee5-bd762cd8921d")
  private val mockUsername: String = "username"
  private val mockAddressId = UUID.fromString("506fdf2f-7c4e-4bc7-bdb6-e42ccbf2a4f4")
  private val mockAlcoholMonitoringConditionsId = UUID.fromString("4f174060-6a26-41d3-ad7d-9b28f607a7df")

  private val mockStartDate: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(2025, 1, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )
  private val mockEndDate: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(2030, 1, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )
  private val mockAlcoholMonitoringConditionsUpdateRecord = UpdateAlcoholMonitoringConditionsDto(
    monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
    startDate = mockStartDate,
    endDate = mockEndDate,
    installationLocation = AlcoholMonitoringInstallationLocationType.PRIMARY,
    prisonName = null,
    probationOfficeName = null,
  )
  private val mockAlcoholMonitoringConditions = AlcoholMonitoringConditions(
    id = mockAlcoholMonitoringConditionsId,
    orderId = mockOrderId,
    monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
    startDate = mockStartDate,
    endDate = mockEndDate,
    installationLocation = AlcoholMonitoringInstallationLocationType.PRIMARY,
    installationAddressId = mockAddressId,
    prisonName = null,
    probationOfficeName = null,
  )

  private val mockAddress = Address(
    id = mockAddressId,
    orderId = mockOrderId,
    addressType = AddressType.PRIMARY,
    addressUsage = DeviceWearerAddressUsage.NA,
    addressLine1 = "mockAddressLine1",
    addressLine2 = "mockAddressLine2",
    postcode = "mockPostcode",
  )

  @BeforeEach
  fun setup() {
    alcoholMonitoringConditionsRepo = Mockito.mock(MonitoringConditionsAlcoholRepository::class.java)
    addressRepo = Mockito.mock(AddressRepository::class.java)
    orderRepo = Mockito.mock(OrderRepository::class.java)
    alcoholMonitoringConditionsService = MonitoringConditionsAlcoholService(orderRepo, alcoholMonitoringConditionsRepo, addressRepo)
  }

  @Nested
  inner class WhenCallingCreateOrUpdateAlcoholMonitoringConditions {
    @Test
    fun `gets the address ID from the order ID and address type`() {
      Mockito.`when`(orderRepo.findByIdAndUsernameAndStatus(mockOrderId, mockUsername, OrderStatus.IN_PROGRESS))
        .thenReturn(
          Optional.of(
            Order(
              id = mockOrderId,
              username = mockUsername,
              status = OrderStatus.IN_PROGRESS,
              monitoringConditionsAlcohol = AlcoholMonitoringConditions(
                id = mockAlcoholMonitoringConditionsId,
                orderId = mockOrderId,
              ),
            ),
          ),
        )
      Mockito.`when`(addressRepo.findByOrderIdAndOrderUsernameAndOrderStatusAndAddressType(mockOrderId, mockUsername, OrderStatus.IN_PROGRESS, AddressType.PRIMARY))
        .thenReturn(Optional.of(mockAddress))
      Mockito.`when`(alcoholMonitoringConditionsRepo.save(mockAlcoholMonitoringConditions))
        .thenReturn(mockAlcoholMonitoringConditions)

      val result = alcoholMonitoringConditionsService.createOrUpdateAlcoholMonitoringConditions(mockOrderId, mockUsername, mockAlcoholMonitoringConditionsUpdateRecord)

      verify(addressRepo, times(1)).findByOrderIdAndOrderUsernameAndOrderStatusAndAddressType(mockOrderId, mockUsername, OrderStatus.IN_PROGRESS, AddressType.PRIMARY)
      verify(alcoholMonitoringConditionsRepo, times(1)).save(mockAlcoholMonitoringConditions)
      org.assertj.core.api.Assertions.assertThat(result).isEqualTo(mockAlcoholMonitoringConditions)
    }

    @Test
    fun `address ID is null when alcohol monitoring installation location does not match an address type`() {
      val mockAlcoholMonitoringConditionsUpdateRecord = UpdateAlcoholMonitoringConditionsDto(
        monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
        startDate = mockStartDate,
        endDate = mockEndDate,
        installationLocation = AlcoholMonitoringInstallationLocationType.PROBATION_OFFICE,
        prisonName = null,
        probationOfficeName = "MockProbationOfficeName",
      )
      val mockAlcoholMonitoringConditions = AlcoholMonitoringConditions(
        id = mockAlcoholMonitoringConditionsId,
        orderId = mockOrderId,
        monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
        startDate = mockStartDate,
        endDate = mockEndDate,
        installationLocation = AlcoholMonitoringInstallationLocationType.PROBATION_OFFICE,
        installationAddressId = null,
        prisonName = null,
        probationOfficeName = "MockProbationOfficeName",
      )
      Mockito.`when`(orderRepo.findByIdAndUsernameAndStatus(mockOrderId, mockUsername, OrderStatus.IN_PROGRESS))
        .thenReturn(
          Optional.of(
            Order(
              id = mockOrderId,
              username = mockUsername,
              status = OrderStatus.IN_PROGRESS,
              monitoringConditionsAlcohol = AlcoholMonitoringConditions(
                id = mockAlcoholMonitoringConditionsId,
                orderId = mockOrderId,
              ),
            ),
          ),
        )
      Mockito.`when`(alcoholMonitoringConditionsRepo.save(mockAlcoholMonitoringConditions))
        .thenReturn(mockAlcoholMonitoringConditions)

      val result = alcoholMonitoringConditionsService.createOrUpdateAlcoholMonitoringConditions(mockOrderId, mockUsername, mockAlcoholMonitoringConditionsUpdateRecord)

      verify(addressRepo, times(0)).findByOrderIdAndOrderUsernameAndOrderStatusAndAddressType(eq(mockOrderId), eq(mockUsername), eq(OrderStatus.IN_PROGRESS), any())
      verify(alcoholMonitoringConditionsRepo, times(1)).save(mockAlcoholMonitoringConditions)
      org.assertj.core.api.Assertions.assertThat(result).isEqualTo(mockAlcoholMonitoringConditions)
    }
  }
}
