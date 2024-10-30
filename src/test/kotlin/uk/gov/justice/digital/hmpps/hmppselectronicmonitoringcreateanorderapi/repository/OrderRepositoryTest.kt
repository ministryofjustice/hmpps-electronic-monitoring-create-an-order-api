package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@DataJpaTest
class OrderRepositoryTest {
  @Autowired
  lateinit var orderRepo: OrderRepository

  private val mockOrderId: UUID = UUID.fromString("da69b6d1-fb7f-4513-aee5-bd762cd8921d")
  private val mockUsername: String = "username"
  private val mockAddressId = UUID.fromString("506fdf2f-7c4e-4bc7-bdb6-e42ccbf2a4f4")
  private val mockAlcoholMonitoringConditionsId = UUID.fromString(
    "4f174060-6a26-41d3-ad7d-9b28f607a7df",
  )

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

  private val mockAddress = Address(
    id = mockAddressId,
    orderId = mockOrderId,
    addressType = AddressType.PRIMARY,
    addressUsage = DeviceWearerAddressUsage.NA,
    addressLine1 = "mockAddressLine1",
    addressLine2 = "mockAddressLine2",
    addressLine3 = "",
    addressLine4 = "",
    postcode = "mockPostcode",
  )

  private val updatedMockAddress = Address(
    id = mockAddressId,
    orderId = mockOrderId,
    addressType = AddressType.PRIMARY,
    addressUsage = DeviceWearerAddressUsage.NA,
    addressLine1 = "updatedMockAddressLine1",
    addressLine2 = "updatedMockAddressLine2",
    addressLine3 = "",
    addressLine4 = "",
    postcode = "updatedMockPostcode",
  )

  private val orderWithExistingPrimaryAddressAndRelation = Order(
    id = mockOrderId,
    username = mockUsername,
    status = OrderStatus.IN_PROGRESS,
    monitoringConditionsAlcohol = AlcoholMonitoringConditions(
      id = mockAlcoholMonitoringConditionsId,
      orderId = mockOrderId,
      monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
      startDate = mockStartDate,
      endDate = mockEndDate,
      installationLocation = AlcoholMonitoringInstallationLocationType.PRIMARY,
      installationAddressId = mockAddressId,
      prisonName = null,
      probationOfficeName = null,
    ),
    addresses = mutableListOf(mockAddress),
  )

  private val updatedOrderWithExistingPrimaryAddressAndRelation = Order(
    id = mockOrderId,
    username = mockUsername,
    status = OrderStatus.IN_PROGRESS,
    monitoringConditionsAlcohol = AlcoholMonitoringConditions(
      id = mockAlcoholMonitoringConditionsId,
      orderId = mockOrderId,
      monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
      startDate = mockStartDate,
      endDate = mockEndDate,
      installationLocation = AlcoholMonitoringInstallationLocationType.PRIMARY,
      installationAddressId = mockAddressId,
      prisonName = null,
      probationOfficeName = null,
    ),
    addresses = mutableListOf(updatedMockAddress),
  )

  fun databaseInsert() {
    orderRepo.save(orderWithExistingPrimaryAddressAndRelation)
  }

  @Nested
  inner class WhenSavingAnOrderThanUpdatesAnAddress {
    @Test
    fun `does not throw an error when other models are linked to that address`() {
      databaseInsert()

      assertDoesNotThrow {
        orderRepo.save(updatedOrderWithExistingPrimaryAddressAndRelation)
      }
      assertDoesNotThrow {
        orderRepo.findByIdAndUsernameAndStatus(mockOrderId, mockUsername, OrderStatus.IN_PROGRESS)
      }
    }
  }
}
