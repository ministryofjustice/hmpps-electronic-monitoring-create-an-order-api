package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.PrisonerDetailsApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.PrisonerRecord
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID

val mockVersionId = UUID.randomUUID()
val baseDetails = PrisonerRecord(
  deviceWearer = null,
  contactDetails = null,
  addresses = emptyList(),
)

class DeviceWearerDetailsServiceTest {

  class TestClient : PrisonerDetailsApi {
    var prisonDetailsResponse = baseDetails.copy()
    var lastRequestedVersionId: UUID? = null

    fun setMockResponse(value: PrisonerRecord) {
      prisonDetailsResponse = value
    }

    override fun getPrisonerDetails(prisonNumber: String, versionId: UUID): PrisonerRecord {
      lastRequestedVersionId = versionId
      return prisonDetailsResponse
    }
  }

  var client: TestClient = TestClient()

  @BeforeEach
  fun setup() {
    client = TestClient()
  }

  @Nested
  @DisplayName("Get details")
  inner class GetDetails {
    @Test
    fun `returns bob as first name when set to bob`() {
      client.setMockResponse(
        baseDetails.copy(
          deviceWearer = DeviceWearer(
            versionId = mockVersionId,
            firstName = "Bob",
          ),
        ),
      )

      val service = DeviceWearerDetailsService(client)

      val res = service.getDetailsOverview("1234")

      assertThat(res.firstName).isEqualTo("Bob")
    }

    @Test
    fun `returns cat as first name when set to cat`() {
      client.setMockResponse(
        baseDetails.copy(
          deviceWearer = DeviceWearer(
            versionId = mockVersionId,
            firstName = "Cat",
          ),
        ),
      )

      val service = DeviceWearerDetailsService(client)

      val res = service.getDetailsOverview("1234")

      assertThat(res.firstName).isEqualTo("Cat")
    }

    @Test
    fun `returns basic prison details`() {
      client.setMockResponse(
        baseDetails.copy(
          deviceWearer = DeviceWearer(
            versionId = mockVersionId,
            firstName = "Bob",
            lastName = "Builder",
            dateOfBirth = ZonedDateTime.of(LocalDateTime.of(1990, 8, 21, 0, 0), ZoneId.of("Europe/London")),
          ),
        ),
      )

      val service = DeviceWearerDetailsService(client)

      val res = service.getDetailsOverview("1234")

      assertThat(res.firstName).isEqualTo("Bob")
      assertThat(res.lastName).isEqualTo("Builder")
      assertThat(res.dateOfBirth).isEqualTo(
        ZonedDateTime.of(
          LocalDateTime.of(1990, 8, 21, 0, 0),
          ZoneId.of("Europe/London"),
        ),
      )
      assertThat(res.firstName).isEqualTo("Bob")
    }
  }

  @Nested
  @DisplayName("Store details")
  inner class StoreDetails {
    private val mockOrderRepo: OrderRepository = mock()
    private lateinit var service: DeviceWearerDetailsService

    private lateinit var mockOrder: Order
    private val mockOrderId: UUID = UUID.randomUUID()
    private val mockVersionId: UUID = UUID.randomUUID()
    private val mockUsername: String = "mockUsername"

    @BeforeEach
    fun setup() {
      service = DeviceWearerDetailsService(client)
      service.orderRepo = mockOrderRepo

      mockOrder = Order(
        id = mockOrderId,
        versions = mutableListOf(
          OrderVersion(
            id = mockVersionId,
            orderId = UUID.randomUUID(),
            username = mockUsername,
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            dataDictionaryVersion = DataDictionaryVersion.DDV6,
          ),
        ),
      )

      whenever(mockOrderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
      whenever(mockOrderRepo.save(mockOrder)).thenReturn(mockOrder)
    }

    @Test
    fun `returns success message when stored`() {
      val response = service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(response.error).isNull()
      assertThat(response.success).isEqualTo(true)
    }

    @Test
    fun `requests prisoner details using the order's versionId`() {
      service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(client.lastRequestedVersionId).isEqualTo(mockVersionId)
    }

    @Test
    fun `returns failure when storage failed`() {
      whenever(mockOrderRepo.findById(mockOrderId)).thenThrow(EntityNotFoundException("Not found"))

      val response = service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(response.success).isEqualTo(false)
      assertThat(response.error).isNotNull
      assertThat(response.error?.message).isEqualTo("Not found")
    }

    @Test
    fun `adds device wearer details`() {
      client.setMockResponse(
        baseDetails.copy(
          deviceWearer = DeviceWearer(
            versionId = mockVersionId,
            firstName = "John",
            lastName = "Deer",
          ),
        ),
      )

      service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(mockOrder.deviceWearer?.firstName).isEqualTo("John")
      assertThat(mockOrder.deviceWearer?.lastName).isEqualTo("Deer")
    }

    @Test
    fun `adds contact details`() {
      client.setMockResponse(
        baseDetails.copy(
          contactDetails = ContactDetails(
            versionId = mockVersionId,
            phoneNumberAvailable = true,
            contactNumber = "01234567890",
          ),
        ),
      )

      service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(mockOrder.contactDetails?.contactNumber).isEqualTo("01234567890")
      assertThat(mockOrder.contactDetails?.phoneNumberAvailable).isEqualTo(true)
    }

    @Test
    fun `adds addresses`() {
      client.setMockResponse(
        baseDetails.copy(
          addresses = listOf(
            Address(
              versionId = mockVersionId,
              postcode = "AB12CD",
              addressType = AddressType.PRIMARY,
              addressLine1 = "line one",
              addressLine2 = "line two",
              addressLine3 = "line three",
              addressLine4 = "line four",
            ),
            Address(
              versionId = mockVersionId,
              postcode = "AB34CD",
              addressType = AddressType.SECONDARY,
              addressLine1 = "line one",
              addressLine2 = "line two",
              addressLine3 = "line three",
              addressLine4 = "line four",
            ),
          ),
        ),
      )

      service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(mockOrder.addresses).isNotEmpty
      assertThat(mockOrder.addresses.size).isEqualTo(2)
      assertThat(mockOrder.addresses.any { it.addressType == AddressType.PRIMARY }).isTrue
      assertThat(mockOrder.addresses.any { it.addressType == AddressType.SECONDARY }).isTrue
    }
  }
}
