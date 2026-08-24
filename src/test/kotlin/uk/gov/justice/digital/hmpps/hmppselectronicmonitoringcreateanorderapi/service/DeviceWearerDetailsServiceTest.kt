package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.CorePersonRecordApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordAuthorisationException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordDependencyException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID

val mockVersionId = UUID.randomUUID()
val baseDetails = CorePersonRecord(
  deviceWearer = null,
  contactDetails = null,
  addresses = emptyList(),
)

class DeviceWearerDetailsServiceTest {

  class TestClient : CorePersonRecordApi {
    var prisonDetailsResponse = baseDetails.copy()
    var lastRequestedVersionId: UUID? = null
    var lastRequestedIdentifier: String? = null
    var lastRoute: String? = null
    var errorToThrow: Exception? = null

    fun setMockResponse(value: CorePersonRecord) {
      prisonDetailsResponse = value
    }

    override fun getPersonByPrisonNumber(prisonNumber: String, versionId: UUID): CorePersonRecord {
      lastRequestedVersionId = versionId
      lastRequestedIdentifier = prisonNumber
      lastRoute = "prison"
      errorToThrow?.let { throw it }
      return prisonDetailsResponse
    }

    override fun getPersonByCrn(crn: String, versionId: UUID): CorePersonRecord {
      lastRequestedVersionId = versionId
      lastRequestedIdentifier = crn
      lastRoute = "probation"
      errorToThrow?.let { throw it }
      return prisonDetailsResponse
    }

    override fun getPersonByDefendantId(defendantId: String, versionId: UUID): CorePersonRecord {
      lastRequestedVersionId = versionId
      lastRequestedIdentifier = defendantId
      lastRoute = "court"
      errorToThrow?.let { throw it }
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

      val res = service.getDetailsOverview("1234", NotifyingOrganisationDDv5.PRISON.name)

      assertThat(res.firstName).isEqualTo("Bob")
      assertThat(res.organisationSearchId).isEqualTo("1234")
      assertThat(client.lastRoute).isEqualTo("prison")
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

      val res = service.getDetailsOverview("1234", NotifyingOrganisationDDv5.PROBATION.name)

      assertThat(res.firstName).isEqualTo("Cat")
      assertThat(client.lastRoute).isEqualTo("probation")
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

      val res = service.getDetailsOverview("1234", NotifyingOrganisationDDv5.MAGISTRATES_COURT.name)

      assertThat(res.firstName).isEqualTo("Bob")
      assertThat(res.lastName).isEqualTo("Builder")
      assertThat(res.dateOfBirth).isEqualTo(
        ZonedDateTime.of(
          LocalDateTime.of(1990, 8, 21, 0, 0),
          ZoneId.of("Europe/London"),
        ),
      )
      assertThat(res.firstName).isEqualTo("Bob")
      assertThat(client.lastRoute).isEqualTo("court")
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
      mockOrder.interestedParties = InterestedParties(versionId = mockVersionId)

      whenever(mockOrderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
      whenever(mockOrderRepo.save(mockOrder)).thenReturn(mockOrder)
    }

    @Test
    fun `returns success message when stored`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      val response = service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(response.error).isNull()
      assertThat(response.success).isEqualTo(true)
    }

    @Test
    fun `requests prisoner details using the order's versionId`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(client.lastRequestedVersionId).isEqualTo(mockVersionId)
    }

    @Test
    fun `returns failure when storage failed`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      whenever(mockOrderRepo.findById(mockOrderId)).thenThrow(EntityNotFoundException("Not found"))

      val response = service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(response.success).isEqualTo(false)
      assertThat(response.error).isNotNull
      assertThat(response.error?.message).isEqualTo("Not found")
    }

    @Test
    fun `adds device wearer details`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
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
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
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
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
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

    @Test
    fun `routes prison lookups to prison endpoint`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name

      service.storeDetails(" A1234BC ", mockOrderId, mockUsername)

      assertThat(client.lastRoute).isEqualTo("prison")
      assertThat(client.lastRequestedIdentifier).isEqualTo("A1234BC")
    }

    @Test
    fun `routes probation lookups to probation endpoint`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PROBATION.name

      service.storeDetails("X12345", mockOrderId, mockUsername)

      assertThat(client.lastRoute).isEqualTo("probation")
      assertThat(client.lastRequestedIdentifier).isEqualTo("X12345")
    }

    @Test
    fun `routes court lookups to common platform endpoint`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.CROWN_COURT.name

      service.storeDetails("defendant-id", mockOrderId, mockUsername)

      assertThat(client.lastRoute).isEqualTo("court")
      assertThat(client.lastRequestedIdentifier).isEqualTo("defendant-id")
    }

    @Test
    fun `throws bad request when notifying organisation is unsupported`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE.name

      assertThatThrownBy {
        service.storeDetails("value", mockOrderId, mockUsername)
      }.hasMessageContaining("unsupported")
    }

    @Test
    fun `returns not found when core person record has no match`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      client.errorToThrow = WebClientResponseException.create(
        404,
        "Not Found",
        HttpHeaders.EMPTY,
        ByteArray(0),
        null,
      )

      val response = service.storeDetails("A1234BC", mockOrderId, mockUsername)

      assertThat(response.success).isFalse
      assertThat(response.error).isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `returns auth dependency failure when core person record returns forbidden`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      client.errorToThrow = WebClientResponseException.create(
        403,
        "Forbidden",
        HttpHeaders.EMPTY,
        ByteArray(0),
        null,
      )

      val response = service.storeDetails("A1234BC", mockOrderId, mockUsername)

      assertThat(response.success).isFalse
      assertThat(response.error).isInstanceOf(CorePersonRecordAuthorisationException::class.java)
    }

    @Test
    fun `returns dependency failure when core person record returns server error`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      client.errorToThrow = WebClientResponseException.create(
        500,
        "Server Error",
        HttpHeaders.EMPTY,
        ByteArray(0),
        null,
      )

      val response = service.storeDetails("A1234BC", mockOrderId, mockUsername)

      assertThat(response.success).isFalse
      assertThat(response.error).isInstanceOf(CorePersonRecordDependencyException::class.java)
      assertThat((response.error as CorePersonRecordDependencyException).upstreamStatusCode)
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }
  }
}
