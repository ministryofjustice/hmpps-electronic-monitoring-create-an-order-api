package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationLocationDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.*

@ActiveProfiles("test")
@JsonTest
class InstallationLocationServiceTest {

  lateinit var repo: OrderRepository

  lateinit var service: InstallationLocationService

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
      ),
    ),
  )

  @BeforeEach
  fun setup() {
    repo = mock(OrderRepository::class.java)
    service = InstallationLocationService()
    service.orderRepo = repo
  }

  @Test
  fun `Should throw exception when order is not found`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.empty())

    assertThrows(EntityNotFoundException::class.java) {
      service.createOrUpdateInstallationLocation(
        mockOrderId,
        mockUsername,
        UpdateInstallationLocationDto(location = InstallationLocationType.INSTALLATION),
      )
    }
  }

  @Test
  fun `Should throw exception when order is already submitted`() {
    mockOrder.status = OrderStatus.SUBMITTED
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))

    assertThrows(EntityNotFoundException::class.java) {
      service.createOrUpdateInstallationLocation(
        mockOrderId,
        mockUsername,
        UpdateInstallationLocationDto(location = InstallationLocationType.INSTALLATION),
      )
    }
  }

  @Test
  fun `Should throw exception when order is not created by the user`() {
    mockOrder.versions = mutableListOf(
      OrderVersion(
        id = mockVersionId,
        versionId = 0,
        status = OrderStatus.IN_PROGRESS,
        orderId = mockOrderId,
        type = RequestType.REQUEST,
        username = "Not" + mockUsername,
        dataDictionaryVersion = DataDictionaryVersion.DDV4,
      ),
    )
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))

    assertThrows(EntityNotFoundException::class.java) {
      service.createOrUpdateInstallationLocation(
        mockOrderId,
        mockUsername,
        UpdateInstallationLocationDto(location = InstallationLocationType.INSTALLATION),
      )
    }
  }

  @Test
  fun `Should update order with new installation location and return`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)
    val result = service.createOrUpdateInstallationLocation(
      mockOrderId,
      mockUsername,
      UpdateInstallationLocationDto(location = InstallationLocationType.INSTALLATION),
    )

    assertThat(mockOrder.installationLocation).isNotNull
    assertThat(mockOrder.installationLocation!!.location).isEqualTo(InstallationLocationType.INSTALLATION)
    assertThat(result.location).isEqualTo(InstallationLocationType.INSTALLATION)
  }

  @Test
  fun `Should clear old data when switching to primary address`() {
    mockOrder.installationAppointment = InstallationAppointment(versionId = mockVersionId)
    mockOrder.addresses.add(
      Address(
        versionId = mockVersionId,
        addressType = AddressType.INSTALLATION,
        addressLine1 = "Mock place",
        addressLine2 = "",
        addressLine3 = "Mock Town",
        postcode = "Mock postcode",
      ),
    )

    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)
    val result = service.createOrUpdateInstallationLocation(
      mockOrderId,
      mockUsername,
      UpdateInstallationLocationDto(location = InstallationLocationType.PRIMARY),
    )

    assertThat(mockOrder.installationAppointment).isNull()
    assertThat(mockOrder.addresses.any { it.addressType == AddressType.INSTALLATION }).isFalse()
  }
}
