import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OffenceService
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
class OffenceServiceTest {
  lateinit var repo: OrderRepository

  lateinit var service: OffenceService

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
    repo = Mockito.mock(OrderRepository::class.java)
    service = OffenceService()
    service.orderRepo = repo
  }

  @Test
  fun `should update the order with the new dapo data`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    val dto = UpdateOffenceDto(offenceType = "type", offenceDate = ZonedDateTime.now())
    val result = service.addOffence(
      mockOrderId,
      mockUsername,
      dto,
    )

    assertThat(result).isNotNull
    assertThat(result.offenceType).isEqualTo(dto.offenceType)
    assertThat(result.offenceDate).isEqualTo(dto.offenceDate)
  }
}
