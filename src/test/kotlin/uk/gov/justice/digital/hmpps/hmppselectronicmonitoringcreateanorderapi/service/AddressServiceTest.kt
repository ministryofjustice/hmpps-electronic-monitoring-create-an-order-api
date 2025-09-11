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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateAddressDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
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
class AddressServiceTest {
  @MockBean
  lateinit var orderRepo: OrderRepository

  @Autowired
  lateinit var addressService: AddressService

  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockVersionId: UUID = UUID.randomUUID()
  private val mockUsername: String = "username"
  private val mockAddressId = UUID.randomUUID()
  private val mockAlcoholMonitoringConditionsId = UUID.randomUUID()
  private val mockDictionaryVersion = DataDictionaryVersion.DDV4

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
    versionId = mockVersionId,
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
    versionId = mockVersionId,
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
          monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
          startDate = mockStartDate,
          endDate = mockEndDate,
        ),
        addresses = mutableListOf(mockAddress),
        dataDictionaryVersion = mockDictionaryVersion,
      ),
    ),
  )

  private val updatedOrderWithExistingPrimaryAddressAndRelation = Order(
    id = mockOrderId,
    versions = mutableListOf(
      OrderVersion(
        id = mockVersionId,
        username = mockUsername,
        status = OrderStatus.IN_PROGRESS,
        type = RequestType.REQUEST,
        versionId = 0,
        orderId = mockOrderId,
        monitoringConditionsAlcohol = AlcoholMonitoringConditions(
          id = mockAlcoholMonitoringConditionsId,
          versionId = mockVersionId,
          monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
          startDate = mockStartDate,
          endDate = mockEndDate,
        ),
        addresses = mutableListOf(updatedMockAddress),
        dataDictionaryVersion = mockDictionaryVersion,
      ),
    ),
  )

  private val orderWithoutPrimaryAddress = Order(
    id = mockOrderId,
    versions = mutableListOf(
      OrderVersion(
        id = mockVersionId,
        username = mockUsername,
        status = OrderStatus.IN_PROGRESS,
        type = RequestType.REQUEST,
        orderId = mockOrderId,
        addresses = mutableListOf(),
        dataDictionaryVersion = mockDictionaryVersion,
      ),
    ),
  )

  @Nested
  inner class WhenCallingUpdateAddress {
    @Test
    fun `updates the existing address record if an address of that type exists for the order`() {
      whenever(
        orderRepo.findById(mockOrderId),
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
      Assertions.assertThat(updatedAddress.id).isEqualTo(originalPrimaryAddressId)
      verify(orderRepo, times(1)).save(updatedOrderWithExistingPrimaryAddressAndRelation)
    }

    @Test
    fun `creates a new address record if an address of that type does not exist for the order`() {
      whenever(
        orderRepo.findById(mockOrderId),
      ).thenReturn(
        Optional.of(orderWithoutPrimaryAddress),
      )

      val addedPrimaryAddress = addressService.updateAddress(
        mockOrderId,
        mockUsername,
        mockAddressUpdateRecord,
      )

      Assertions.assertThat(
        addedPrimaryAddress.addressType,
      ).isEqualTo(updatedMockAddress.addressType)
      Assertions.assertThat(
        addedPrimaryAddress.addressLine1,
      ).isEqualTo(updatedMockAddress.addressLine1)
      Assertions.assertThat(addedPrimaryAddress.postcode).isEqualTo(updatedMockAddress.postcode)
    }

    @Test
    fun `should update primary address and clear secondary, tertiary address when user indicates no more`() {
      val secondaryAddress = Address(
        id = UUID.randomUUID(),
        versionId = mockVersionId,
        addressType = AddressType.SECONDARY,
        addressLine1 = "Mock Secondary Address Line 1",
        addressLine2 = "Mock Secondary Address Line 2",
        postcode = "SS1 2SS",
      )
      val tertiaryAddress = Address(
        id = UUID.randomUUID(),
        versionId = mockVersionId,
        addressType = AddressType.TERTIARY,
        addressLine1 = "Mock Tertiary Address Line 1",
        addressLine2 = "Mock Tertiary Address Line 2",
        postcode = "TT1 2TT",
      )

      orderWithExistingPrimaryAddressAndRelation.addresses.addAll(listOf(secondaryAddress, tertiaryAddress))

      whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(orderWithExistingPrimaryAddressAndRelation))

      val mockUpdateRecord = UpdateAddressDto(
        addressType = AddressType.PRIMARY,
        addressLine1 = "updatedMockAddressLine1",
        addressLine2 = "updatedMockAddressLine2",
        addressLine3 = "",
        addressLine4 = "",
        postcode = "updatedMockPostcode",
        hasAnotherAddress = false,
      )

      addressService.updateAddress(mockOrderId, mockUsername, mockUpdateRecord)

      val savedAddresses = orderWithExistingPrimaryAddressAndRelation.addresses

      Assertions.assertThat(savedAddresses).hasSize(1)

      val remainingAddress = savedAddresses.first()
      Assertions.assertThat(remainingAddress.addressType).isEqualTo(AddressType.PRIMARY)
      Assertions.assertThat(remainingAddress.addressLine1).isEqualTo(mockUpdateRecord.addressLine1)
    }

    @Test
    fun `should update secondary address and clear only tertiary address when user indicates no more`() {
      val secondaryAddress = Address(
        id = UUID.randomUUID(),
        versionId = mockVersionId,
        addressType = AddressType.SECONDARY,
        addressLine1 = "Mock Secondary Address Line 1",
        addressLine2 = "Mock Secondary Address Line 2",
        postcode = "SS1 2SS",
      )
      val tertiaryAddress = Address(
        id = UUID.randomUUID(),
        versionId = mockVersionId,
        addressType = AddressType.TERTIARY,
        addressLine1 = "Mock Tertiary Address Line 1",
        addressLine2 = "Mock Tertiary Address Line 2",
        postcode = "TT1 2TT",
      )

      orderWithExistingPrimaryAddressAndRelation.addresses.addAll(listOf(secondaryAddress, tertiaryAddress))
      whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(orderWithExistingPrimaryAddressAndRelation))

      val mockUpdateRecord = UpdateAddressDto(
        addressType = AddressType.SECONDARY,
        addressLine1 = "Updated Mock Secondary Address Line 1",
        addressLine2 = "UpdatedMock Secondary Address Line 2",
        postcode = "SS2 2SS",
        hasAnotherAddress = false,
      )

      addressService.updateAddress(mockOrderId, mockUsername, mockUpdateRecord)

      val savedAddresses = orderWithExistingPrimaryAddressAndRelation.addresses
      val primaryAddress = savedAddresses.find { it.addressType == AddressType.PRIMARY }
      val updatedSecondary = savedAddresses.find { it.addressType == AddressType.SECONDARY }

      Assertions.assertThat(savedAddresses).hasSize(2)

      Assertions.assertThat(primaryAddress?.addressLine1).isEqualTo(mockAddress.addressLine1)
      Assertions.assertThat(primaryAddress?.postcode).isEqualTo(mockAddress.postcode)

      Assertions.assertThat(savedAddresses).extracting("addressType")
        .containsExactlyInAnyOrder(AddressType.PRIMARY, AddressType.SECONDARY)
      Assertions.assertThat(updatedSecondary?.addressLine1).isEqualTo(mockUpdateRecord.addressLine1)
    }
  }
}
