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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.PrisonerDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID

val baseDetails = PrisonerDetails(
  firstName = null,
  middleNames = null,
  lastName = null,
  dateOfBirth = null,
  sex = null,
  identifiers = null,
  aliases = emptyList(),
  addresses = emptyList(),
)

class DeviceWearerDetailsServiceTest {

  class TestClient : PrisonerDetailsApi {
    var prisonDetailsResponse = baseDetails.copy()

    fun setMockResponse(value: PrisonerDetails) {
      prisonDetailsResponse = value
    }

    override fun getPrisonerDetails(prisonNumber: String): PrisonerDetails = prisonDetailsResponse
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
      client.setMockResponse(baseDetails.copy(firstName = "Bob"))

      val service = DeviceWearerDetailsService(client)

      val res = service.getDetailsOverview("1234")

      assertThat(res.firstName).isEqualTo("Bob")
    }

    @Test
    fun `returns cat as first name when set to cat`() {
      client.setMockResponse(baseDetails.copy(firstName = "Cat"))

      val service = DeviceWearerDetailsService(client)

      val res = service.getDetailsOverview("1234")

      assertThat(res.firstName).isEqualTo("Cat")
    }

    @Test
    fun `returns basic prison details`() {
      client.setMockResponse(baseDetails.copy(firstName = "Bob", lastName = "Builder", dateOfBirth = "1990-08-21"))

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
    fun `returns failure when storage failed`() {
      whenever(mockOrderRepo.findById(mockOrderId)).thenThrow(EntityNotFoundException("Not found"))

      val response = service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(response.success).isEqualTo(false)
      assertThat(response.error).isNotNull
      assertThat(response.error?.message).isEqualTo("Not found")
    }
  }
}
