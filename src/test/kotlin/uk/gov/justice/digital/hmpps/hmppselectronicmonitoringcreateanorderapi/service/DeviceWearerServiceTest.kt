package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDeviceWearerDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateNoFixedAbodeDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID

@ActiveProfiles("test")
@JsonTest
class DeviceWearerServiceTest {

  private lateinit var service: DeviceWearerService
  private lateinit var orderRepo: OrderRepository
  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockVersionId: UUID = UUID.randomUUID()
  private val mockUsername: String = "username"
  private val mockOrder = Order(
    id = mockOrderId,
    versions = mutableListOf(
      OrderVersion(
        id = mockVersionId,
        versionId = 0,
        status = OrderStatus.IN_PROGRESS,
        orderId = mockOrderId,
        type = RequestType.REQUEST,
        username = mockUsername,
        dataDictionaryVersion = DataDictionaryVersion.DDV4,
        deviceWearerResponsibleAdult = ResponsibleAdult(
          versionId = mockVersionId,

        ),
      ),
    ),
  )

  @BeforeEach
  fun setup() {
    orderRepo = mock(OrderRepository::class.java)
    service = DeviceWearerService()
    service.orderRepo = orderRepo
  }

  @Test
  fun `Should update device wearer`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)
    val mockUpdateRecord = UpdateDeviceWearerDto(
      firstName = "firstName",
      lastName = "lastName",
      alias = "alias",
      adultAtTimeOfInstallation = true,
      sex = "MALE",
      gender = "MALE",
      dateOfBirth = ZonedDateTime.of(1980, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC")),
      disabilities = null,
      interpreterRequired = true,
      language = "en",
    )
    service.updateDeviceWearer(mockOrderId, mockUsername, mockUpdateRecord)

    assertThat(mockOrder.deviceWearer?.firstName).isEqualTo(mockUpdateRecord.firstName)
    assertThat(mockOrder.deviceWearer?.lastName).isEqualTo(mockUpdateRecord.lastName)
    assertThat(mockOrder.deviceWearer?.alias).isEqualTo(mockUpdateRecord.alias)
    assertThat(mockOrder.deviceWearer?.gender).isEqualTo(mockUpdateRecord.gender)
    assertThat(mockOrder.deviceWearer?.sex).isEqualTo(mockUpdateRecord.sex)
    assertThat(mockOrder.deviceWearer?.adultAtTimeOfInstallation).isTrue
    assertThat(mockOrder.deviceWearer?.dateOfBirth).isEqualTo(mockUpdateRecord.dateOfBirth)
    assertThat(mockOrder.deviceWearer?.disabilities).isEqualTo(mockUpdateRecord.disabilities)
    assertThat(mockOrder.deviceWearer?.interpreterRequired).isTrue
    assertThat(mockOrder.deviceWearer?.language).isEqualTo(mockUpdateRecord.language)
  }

  @Test
  fun `Should clear responsible adult when update record adultAtTimeOfInstallation is true`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)
    val mockUpdateRecord = UpdateDeviceWearerDto(
      firstName = "firstName",
      lastName = "lastName",
      alias = "alias",
      adultAtTimeOfInstallation = true,
      sex = "MALE",
      gender = "MALE",
      dateOfBirth = ZonedDateTime.of(1980, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC")),
      disabilities = null,
      interpreterRequired = false,
    )

    service.updateDeviceWearer(mockOrderId, mockUsername, mockUpdateRecord)
    assertThat(mockOrder.deviceWearerResponsibleAdult).isNull()
  }

  @Test
  fun `Should clear primary, secondary and tertiary addresses when noFixedAbode is updated from false to true`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)
    mockOrder.deviceWearer = DeviceWearer(
      versionId = mockVersionId,
      firstName = "MockFirstName",
      lastName = "MockLastName",
    )

    mockOrder.addresses.addAll(
      listOf(
        Address(
          versionId = mockVersionId,
          addressType = AddressType.PRIMARY,
          addressLine1 = "MockPrimary",
          addressLine2 = "",
          postcode = "MockPrimaryPostcode",
        ),
        Address(
          versionId = mockVersionId,
          addressType = AddressType.SECONDARY,
          addressLine1 = "MockSecondary",
          addressLine2 = "",
          postcode = "MockSecondaryPostcode",
        ),
        Address(
          versionId = mockVersionId,
          addressType = AddressType.TERTIARY,
          addressLine1 = "MockTertiary",
          addressLine2 = "",
          postcode = "MockTertiaryPostcode",
        ),
        Address(
          versionId = mockVersionId,
          addressType = AddressType.INSTALLATION,
          addressLine1 = "mockInstallation",
          addressLine2 = "",
          postcode = "MockInstallationPostcode",
        ),
      ),
    )

    val mockUpdateRecord = UpdateNoFixedAbodeDto(
      noFixedAbode = true,
    )

    service.updateNoFixedAbode(mockOrderId, mockUsername, mockUpdateRecord)

    val remainingAddresses = mockOrder.addresses
    assertThat(mockOrder.deviceWearer?.noFixedAbode).isTrue

    assertThat(remainingAddresses).hasSize(1)
    assertThat(remainingAddresses.first().addressType).isEqualTo(AddressType.INSTALLATION)
    assertThat(remainingAddresses).noneMatch {
      it.addressType == AddressType.PRIMARY ||
        it.addressType == AddressType.SECONDARY ||
        it.addressType == AddressType.TERTIARY
    }
  }
}
