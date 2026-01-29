import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DetailsOfInstallationService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.UpdateDetailsOfInstallationDto
import java.util.*

@ActiveProfiles("test")
class DetailsOfInstallationServiceTest {
  private val mockOrderRepo: OrderRepository = mock()
  private lateinit var service: DetailsOfInstallationService

  private lateinit var mockOrder: Order
  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockVersionId: UUID = UUID.randomUUID()
  private val mockUsername: String = "mockUsername"

  @BeforeEach
  fun setup() {
    service = DetailsOfInstallationService()
    service.orderRepo = mockOrderRepo

    mockOrder = Order(
      id = mockOrderId,
      versions = mutableListOf(
        OrderVersion(
          id = mockVersionId,
          orderId = mockOrderId,
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
  fun `should be able to update details of installation with empty`() {
    val mockUpdateDto = UpdateDetailsOfInstallationDto(
      riskCategory = arrayOf<String>(),
      riskDetails = "",
    )

    service.updateDetailsOfInstallation(mockOrderId, mockUsername, mockUpdateDto)

    assertThat(mockOrder.detailsOfInstallation?.riskCategory).isEqualTo(emptyArray<String>())
    assertThat(mockOrder.detailsOfInstallation?.riskDetails).isEqualTo("")
  }

  @Test
  fun `should be able to update details of installation with values`() {
    val mockUpdateDto = UpdateDetailsOfInstallationDto(
      riskCategory = arrayOf("THREATS_OF_VIOLENCE", "SEXUAL_OFFENCES"),
      riskDetails = "some risk details",
    )

    service.updateDetailsOfInstallation(mockOrderId, mockUsername, mockUpdateDto)

    assertThat(mockOrder.detailsOfInstallation?.riskCategory).isEqualTo(
      arrayOf(
        "THREATS_OF_VIOLENCE",
        "SEXUAL_OFFENCES",
      ),
    )
    assertThat(mockOrder.detailsOfInstallation?.riskDetails).isEqualTo("some risk details")
  }
}
