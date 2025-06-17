package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateAlcoholMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
class MonitoringConditionsAlcoholServiceTest {
  @MockBean
  lateinit var orderRepo: OrderRepository

  @Autowired
  lateinit var alcoholMonitoringConditionsService: MonitoringConditionsAlcoholService

  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockVersionId: UUID = UUID.randomUUID()
  private val mockUsername: String = "username"
  private val mockAddressId = UUID.randomUUID()
  private val mockAlcoholMonitoringConditionsId = UUID.randomUUID()

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
    installationLocation = InstallationLocationType.PRIMARY,
    prisonName = null,
    probationOfficeName = null,
  )

  private val mockAlcoholMonitoringConditions = AlcoholMonitoringConditions(
    id = mockAlcoholMonitoringConditionsId,
    versionId = mockVersionId,
    monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
    startDate = mockStartDate,
    endDate = mockEndDate,
    installationLocation = InstallationLocationType.PRIMARY,
    installationAddressId = mockAddressId,
    prisonName = null,
    probationOfficeName = null,
  )

  private val mockAddress = Address(
    id = mockAddressId,
    versionId = mockVersionId,
    addressType = AddressType.PRIMARY,
    addressUsage = DeviceWearerAddressUsage.NA,
    addressLine1 = "mockAddressLine1",
    addressLine2 = "mockAddressLine2",
    postcode = "mockPostcode",
  )

  @Nested
  inner class WhenCallingCreateOrUpdateAlcoholMonitoringConditions {
    @Test
    fun `gets the address ID from the order ID and address type`() {
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                username = mockUsername,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                orderId = mockOrderId,
                monitoringConditionsAlcohol = AlcoholMonitoringConditions(
                  id = mockAlcoholMonitoringConditionsId,
                  versionId = mockVersionId,
                ),
                addresses = mutableListOf(mockAddress),
              ),
            ),
          ),
        ),
      )
      whenever(
        orderRepo.save(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                username = mockUsername,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                orderId = mockOrderId,
                monitoringConditionsAlcohol = mockAlcoholMonitoringConditions,
                addresses = mutableListOf(mockAddress),
              ),
            ),
          ),
        ),
      ).thenReturn(
        Order(
          id = mockOrderId,
          versions = mutableListOf(
            OrderVersion(
              id = mockVersionId,
              username = mockUsername,
              status = OrderStatus.IN_PROGRESS,
              type = RequestType.REQUEST,
              orderId = mockOrderId,
              monitoringConditionsAlcohol = mockAlcoholMonitoringConditions,
              addresses = mutableListOf(mockAddress),
            ),
          ),
        ),
      )

      val result = alcoholMonitoringConditionsService.createOrUpdateAlcoholMonitoringConditions(
        mockOrderId,
        mockUsername,
        mockAlcoholMonitoringConditionsUpdateRecord,
      )

      assertThat(result).isEqualTo(mockAlcoholMonitoringConditions)
    }

    @Test
    fun `address ID is null when alcohol monitoring installation location does not match an address type`() {
      val mockEndDate = ZonedDateTime.of(
        mockEndDate.year,
        mockEndDate.monthValue,
        mockEndDate.dayOfMonth,
        23,
        59,
        0,
        0,
        ZoneId.of("Europe/London"),
      )
      val mockAlcoholMonitoringConditionsUpdateRecord = UpdateAlcoholMonitoringConditionsDto(
        monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
        startDate = mockStartDate,
        endDate = mockEndDate,
        installationLocation = InstallationLocationType.PROBATION_OFFICE,
        prisonName = null,
        probationOfficeName = "MockProbationOfficeName",
      )
      val mockAlcoholMonitoringConditions = AlcoholMonitoringConditions(
        id = mockAlcoholMonitoringConditionsId,
        versionId = mockVersionId,
        monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
        startDate = mockStartDate,
        endDate = mockEndDate,
        installationLocation = InstallationLocationType.PROBATION_OFFICE,
        installationAddressId = null,
        prisonName = null,
        probationOfficeName = "MockProbationOfficeName",
      )
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                username = mockUsername,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                orderId = mockOrderId,
                monitoringConditionsAlcohol = AlcoholMonitoringConditions(
                  id = mockAlcoholMonitoringConditionsId,
                  versionId = mockVersionId,
                ),
                addresses = mutableListOf(mockAddress),
              ),
            ),
          ),
        ),
      )
      whenever(
        orderRepo.save(
          Order(
            id = mockOrderId,
            versions = mutableListOf(
              OrderVersion(
                id = mockVersionId,
                username = mockUsername,
                status = OrderStatus.IN_PROGRESS,
                type = RequestType.REQUEST,
                orderId = mockOrderId,
                monitoringConditionsAlcohol = mockAlcoholMonitoringConditions,
                addresses = mutableListOf(mockAddress),
              ),
            ),
          ),
        ),
      ).thenReturn(
        Order(
          id = mockOrderId,
          versions = mutableListOf(
            OrderVersion(
              id = mockVersionId,
              username = mockUsername,
              status = OrderStatus.IN_PROGRESS,
              type = RequestType.REQUEST,
              orderId = mockOrderId,
              monitoringConditionsAlcohol = mockAlcoholMonitoringConditions,
              addresses = mutableListOf(mockAddress),
            ),
          ),
        ),
      )

      val result = alcoholMonitoringConditionsService.createOrUpdateAlcoholMonitoringConditions(
        mockOrderId,
        mockUsername,
        mockAlcoholMonitoringConditionsUpdateRecord,
      )

      assertThat(result).isEqualTo(mockAlcoholMonitoringConditions)
    }
  }
}
