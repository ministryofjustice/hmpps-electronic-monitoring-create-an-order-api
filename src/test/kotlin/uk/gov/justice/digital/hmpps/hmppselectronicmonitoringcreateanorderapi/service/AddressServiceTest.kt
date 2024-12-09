package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateAddressDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
class AddressServiceTest {
  @MockBean
  lateinit var orderRepo: OrderRepository

  @Autowired
  lateinit var addressService: AddressService

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

  private val mockAddressUpdateRecord = UpdateAddressDto(
    addressType = AddressType.PRIMARY,
    addressLine1 = "updatedMockAddressLine1",
    addressLine2 = "updatedMockAddressLine2",
    addressLine3 = "",
    addressLine4 = "",
    postcode = "updatedMockPostcode",
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
    type = OrderType.REQUEST,
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
    type = OrderType.REQUEST,
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

  private val orderWithoutPrimaryAddress = Order(
    id = mockOrderId,
    username = mockUsername,
    status = OrderStatus.IN_PROGRESS,
    type = OrderType.REQUEST,
    addresses = mutableListOf(),
  )

  @Nested
  inner class WhenCallingUpdateAddress {
    @Test
    fun `updates the existing address record if an address of that type exists for the order`() {
      whenever(
        orderRepo.findByIdAndUsernameAndStatus(mockOrderId, mockUsername, OrderStatus.IN_PROGRESS),
      ).thenReturn(
        Optional.of(orderWithExistingPrimaryAddressAndRelation),
      )
      val originalPrimaryAddressId = orderWithExistingPrimaryAddressAndRelation.addresses.find {
        it.addressType == AddressType.PRIMARY
      }?.id

      val updatedAddress = addressService.updateAddress(
        mockOrderId,
        mockUsername,
        mockAddressUpdateRecord,
      )

      Assertions.assertThat(updatedAddress).isEqualTo(updatedMockAddress)
      Assertions.assertThat(updatedAddress?.id).isEqualTo(originalPrimaryAddressId)
      verify(orderRepo, times(1)).save(updatedOrderWithExistingPrimaryAddressAndRelation)
    }

    @Test
    fun `creates a new address record if an address of that type does not exist for the order`() {
      whenever(
        orderRepo.findByIdAndUsernameAndStatus(mockOrderId, mockUsername, OrderStatus.IN_PROGRESS),
      ).thenReturn(
        Optional.of(orderWithoutPrimaryAddress),
      )

      val addedPrimaryAddress = addressService.updateAddress(
        mockOrderId,
        mockUsername,
        mockAddressUpdateRecord,
      )

      Assertions.assertThat(
        addedPrimaryAddress?.addressType,
      ).isEqualTo(updatedMockAddress.addressType)
      Assertions.assertThat(
        addedPrimaryAddress?.addressLine1,
      ).isEqualTo(updatedMockAddress.addressLine1)
      Assertions.assertThat(addedPrimaryAddress?.postcode).isEqualTo(updatedMockAddress.postcode)
    }
  }
}
