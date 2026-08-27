package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.CorePersonRecordApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordDependencyException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordNotFoundException
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
  }

  var client: TestClient = TestClient()

  @BeforeEach
  fun setup() {
    client = TestClient()
  }

  @Nested
  @DisplayName("Get details")
  inner class GetDetails {
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
    }

    @Test
    fun `returns bob as first name when set to bob`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      client.setMockResponse(
        baseDetails.copy(
          deviceWearer = DeviceWearer(
            versionId = mockVersionId,
            firstName = "Bob",
          ),
        ),
      )

      val res = service.getDetailsOverview("1234", mockOrderId, mockUsername)

      assertThat(res.firstName).isEqualTo("Bob")
      assertThat(res.organisationSearchId).isEqualTo("1234")
      assertThat(client.lastRoute).isEqualTo("prison")
    }

    @Test
    fun `returns cat as first name when set to cat`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PROBATION.name
      client.setMockResponse(
        baseDetails.copy(
          deviceWearer = DeviceWearer(
            versionId = mockVersionId,
            firstName = "Cat",
          ),
        ),
      )

      val res = service.getDetailsOverview("1234", mockOrderId, mockUsername)

      assertThat(res.firstName).isEqualTo("Cat")
      assertThat(client.lastRoute).isEqualTo("probation")
    }

    @Test
    fun `throws when order does not exist for username`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name

      assertThatThrownBy {
        service.getDetailsOverview("1234", mockOrderId, "someone-else")
      }.isInstanceOf(EntityNotFoundException::class.java)
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
    fun `does not throw when stored successfully`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name

      assertThatCode {
        service.storeDetails("1234", mockOrderId, mockUsername)
      }.doesNotThrowAnyException()
    }

    @Test
    fun `requests prisoner details using the order's versionId`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      service.storeDetails("1234", mockOrderId, mockUsername)

      assertThat(client.lastRequestedVersionId).isEqualTo(mockVersionId)
    }

    @Test
    fun `throws when storage failed`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      whenever(mockOrderRepo.findById(mockOrderId)).thenThrow(EntityNotFoundException("Not found"))

      assertThatThrownBy {
        service.storeDetails("1234", mockOrderId, mockUsername)
      }.isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Not found")
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
    fun `rejects court lookups without a defendant id`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.CROWN_COURT.name

      assertThatThrownBy {
        service.storeDetails("value", mockOrderId, mockUsername)
      }.hasMessageContaining("unsupported")
    }

    @ParameterizedTest
    @EnumSource(
      value = NotifyingOrganisationDDv5::class,
      names = ["PRISON", "YOUTH_CUSTODY_SERVICE", "PROBATION"],
      mode = EnumSource.Mode.EXCLUDE,
    )
    fun `throws bad request when notifying organisation is unsupported`(
      notifyingOrganisation: NotifyingOrganisationDDv5,
    ) {
      mockOrder.interestedParties!!.notifyingOrganisation = notifyingOrganisation.name

      assertThatThrownBy {
        service.storeDetails("value", mockOrderId, mockUsername)
      }.isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("unsupported")
    }

    @Test
    fun `throws not found when core person record has no match`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      client.errorToThrow = CorePersonRecordNotFoundException("No Core Person Record was found for id A1234BC")

      assertThatThrownBy {
        service.storeDetails("A1234BC", mockOrderId, mockUsername)
      }.isInstanceOf(CorePersonRecordNotFoundException::class.java)
    }

    @Test
    fun `propagates dependency failures raised by the core person record gateway`() {
      mockOrder.interestedParties!!.notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name
      client.errorToThrow = CorePersonRecordDependencyException(
        "Core Person Record lookup failed for id A1234BC",
        HttpStatus.INTERNAL_SERVER_ERROR,
      )

      assertThatThrownBy {
        service.storeDetails("A1234BC", mockOrderId, mockUsername)
      }.isInstanceOf(CorePersonRecordDependencyException::class.java)
        .matches {
          (it as CorePersonRecordDependencyException).upstreamStatusCode == HttpStatus.INTERNAL_SERVER_ERROR
        }
    }
  }
}
